// Supabase Edge Function: check_expired_subscriptions
// Description: Runs daily to inspect active SaaS pharmacy accounts, auto-suspends expired subscriptions,
// disabling pharmacy login, disabling patient booking, and inserting a notification for the Master Admin.
// Runtime: Deno / TypeScript

import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.7.1"

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

serve(async (req) => {
  // Respond to preflight requests (CORS check)
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders })
  }

  try {
    // Initialize Supabase Admin Client using service role credentials to bypass RLS
    const supabaseClient = createClient(
      Deno.env.get('SUPABASE_URL') ?? '',
      Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? '',
      {
        auth: {
          persistSession: false,
          autoRefreshToken: false,
        }
      }
    )

    // Current timestamp/date for comparison
    const now = new Date()
    const todayStr = now.toISOString().split('T')[0] // YYYY-MM-DD format

    // Query all pharmacies whose subscriptions have expired but are still 'active'
    const { data: expiredPharmacies, error: fetchError } = await supabaseClient
      .from('pharmacies')
      .select('id, name, subscription_expiry')
      .eq('status', 'active')
      .eq('is_deleted', false)
      .lt('subscription_expiry', todayStr)

    if (fetchError) {
      throw new Error(`Failed to fetch expired subscriptions: ${fetchError.message}`)
    }

    const processedAccounts = []

    if (expiredPharmacies && expiredPharmacies.length > 0) {
      for (const pharmacy of expiredPharmacies) {
        // 1. Suspend Pharmacy in SaaS core ledger
        const { error: pharUpdateError } = await supabaseClient
          .from('pharmacies')
          .update({ 
            status: 'suspended', 
            updated_at: new Date().toISOString() 
          })
          .eq('id', pharmacy.id)

        if (pharUpdateError) {
          console.error(`Error updating status for pharmacy ${pharmacy.id}: ${pharUpdateError.message}`)
          continue
        }

        // 2. Disable pharmacy login (suspend corresponding login profile in profiles table)
        const { error: profileUpdateError } = await supabaseClient
          .from('profiles')
          .update({ 
            status: 'suspended', 
            updated_at: new Date().toISOString() 
          })
          .eq('pharmacy_id', pharmacy.id)

        if (profileUpdateError) {
          console.error(`Error updating profile status for pharmacy ${pharmacy.name}: ${profileUpdateError.message}`)
        }

        // 3. Disable patient booking (disable booking in schedules or mark as suspended)
        // We ensure patient booking is disabled by setting is_open_for_booking = false for all their doctors' schedules
        const { data: doctors, error: doctorError } = await supabaseClient
          .from('doctors')
          .select('id')
          .eq('pharmacy_id', pharmacy.id)

        if (!doctorError && doctors) {
          const doctorIds = doctors.map(d => d.id)
          if (doctorIds.length > 0) {
            const { error: scheduleError } = await supabaseClient
              .from('schedules')
              .update({ is_open_for_booking: false })
              .in('doctor_id', doctorIds)

            if (scheduleError) {
              console.error(`Error disabling schedules/patient booking for pharmacy ${pharmacy.name}: ${scheduleError.message}`)
            }
          }
        }

        // 4. Create direct Audit log of subscription suspension
        await supabaseClient
          .from('audit_logs')
          .insert({
            role: 'system',
            action: 'Subscription Expired: Automatically suspended account and disabled bookings',
            table_name: 'pharmacies',
            record_id: pharmacy.id
          })

        // 5. Insert notification for Master Admin (user_id = NULL targets Master Admin)
        await supabaseClient
          .from('notifications')
          .insert({
            title: 'SaaS Subscription Expired',
            message: `The subscription for "${pharmacy.name}" expired on ${pharmacy.subscription_expiry}. The account has been suspended, login disabled, and active bookings halted.`,
            is_read: false
          })

        processedAccounts.push({
          id: pharmacy.id,
          name: pharmacy.name,
          expiredOn: pharmacy.subscription_expiry
        })
      }
    }

    return new Response(
      JSON.stringify({
        success: true,
        message: `Subscription expiry check completed successfully.`,
        processed_count: processedAccounts.length,
        suspended_accounts: processedAccounts
      }),
      {
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        status: 200,
      }
    )

  } catch (error: any) {
    return new Response(
      JSON.stringify({
        success: false,
        error: error.message
      }),
      {
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        status: 400,
      }
    )
  }
})
