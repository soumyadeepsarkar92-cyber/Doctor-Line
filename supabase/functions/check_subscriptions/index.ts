// Supabase Edge Function: check_subscriptions
// Description: Runs daily to sweep active SaaS pharmacy accounts, auto-suspends accounts past their terms, and launches real-time audit event alerts.
// Runtime: Deno / TypeScript

import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.7.1"

// CORS headers configuration
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
    // 1. Initialize Supabase Admin Client using privileged service role credentials
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

    // 2. Fetch all active pharmacies that have surpassed their contract expiry date
    const today = new Date().toISOString().split('T')[0] // YYYY-MM-DD
    
    const { data: expiredPharmacies, error: fetchError } = await supabaseClient
      .from('pharmacies')
      .select('id, name, subscription_expiry')
      .eq('status', 'active')
      .eq('is_deleted', false)
      .lt('subscription_expiry', today)

    if (fetchError) {
      throw new Error(`Failed to query expired subscriptions: ${fetchError.message}`)
    }

    const processed = []

    if (expiredPharmacies && expiredPharmacies.length > 0) {
      for (const pharmacy of expiredPharmacies) {
        // A. Suspend Pharmacy in SaaS core ledger
        const { error: pharUpdateError } = await supabaseClient
          .from('pharmacies')
          .update({ status: 'suspended', updated_at: new Date().toISOString() })
          .eq('id', pharmacy.id)

        if (pharUpdateError) {
          console.error(`Error updating status for pharmacy ${pharmacy.id}: ${pharUpdateError.message}`)
          continue
        }

        // B. Suspend matching public profiles table for system login block
        const { error: profileUpdateError } = await supabaseClient
          .from('profiles')
          .update({ status: 'suspended', updated_at: new Date().toISOString() })
          .eq('pharmacy_id', pharmacy.id)

        if (profileUpdateError) {
          console.error(`Error updating login profile for pharmacy ${pharmacy.name}: ${profileUpdateError.message}`)
        }

        // C. Create direct Audit log of suspension action
        await supabaseClient
          .from('audit_logs')
          .insert({
            role: 'system',
            action: 'Subscription Validity Terminated: Automated account suspension applied',
            table_name: 'pharmacies',
            record_id: pharmacy.id
          })

        // D. Create notification item to warn Master Admin
        await supabaseClient
          .from('notifications')
          .insert({
            title: 'SaaS Agreement Expired',
            message: `Account for "${pharmacy.name}" has officially expired on ${pharmacy.subscription_expiry} and has been temporarily suspended from all actions.`,
            is_read: false
          })

        processed.push({
          id: pharmacy.id,
          name: pharmacy.name,
          expiredOn: pharmacy.subscription_expiry
        })
      }
    }

    return new Response(
      JSON.stringify({
        success: true,
        message: `Sweep completed on date: ${today}`,
        suspended_accounts_count: processed.length,
        accounts_suspended: processed
      }),
      {
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        status: 200,
      }
    )

  } catch (error) {
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
