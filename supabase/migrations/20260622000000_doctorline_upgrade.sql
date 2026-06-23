-- DoctorLine Complete Supabase Migration Script
-- Version: 20260622_upgrade
-- Target: Production Ready

-- Enable UUID Extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ==========================================
-- 1. MASTER ADMINS TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS public.master_admins (
    id UUID PRIMARY KEY,
    email TEXT UNIQUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- ==========================================
-- 2. PHARMACIES TABLE (SaaS Accounts)
-- ==========================================
CREATE TABLE IF NOT EXISTS public.pharmacies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    owner_name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    phone TEXT NOT NULL,
    address TEXT NOT NULL,
    license TEXT NOT NULL,
    banner_name TEXT DEFAULT 'standard_pharmacy_banner' NOT NULL,
    subscription_plan TEXT NOT NULL DEFAULT 'Basic' CHECK (subscription_plan IN ('Basic', 'Premium', 'Enterprise')),
    subscription_start DATE NOT NULL DEFAULT CURRENT_DATE,
    subscription_expiry DATE NOT NULL,
    subscription_amount DECIMAL(10,2) NOT NULL DEFAULT 499.00 CHECK (subscription_amount >= 0),
    subscription_payment_status TEXT NOT NULL DEFAULT 'Paid' CHECK (subscription_payment_status IN ('Paid', 'Pending')),
    status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'pending', 'suspended', 'rejected')),
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- ==========================================
-- 3. PROFILES TABLE (Linked with Supabase Auth)
-- ==========================================
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID REFERENCES auth.users(id) ON DELETE CASCADE PRIMARY KEY,
    email TEXT UNIQUE NOT NULL,
    role TEXT NOT NULL DEFAULT 'patient' CHECK (role IN ('master_admin', 'pharmacy', 'patient')),
    status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('pending', 'active', 'suspended', 'rejected')),
    pharmacy_id UUID REFERENCES public.pharmacies(id) ON DELETE SET NULL NULL,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- ==========================================
-- 4. DOCTORS TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS public.doctors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    specialization TEXT NOT NULL,
    experience INTEGER NOT NULL CHECK (experience >= 0),
    fee DECIMAL(10,2) NOT NULL CHECK (fee >= 0),
    rating DECIMAL(3,2) NOT NULL DEFAULT 4.50 CHECK (rating >= 0.00 AND rating <= 5.00),
    pharmacy_id UUID REFERENCES public.pharmacies(id) ON DELETE CASCADE NOT NULL,
    banner_name TEXT NOT NULL DEFAULT 'standard_doctor_banner',
    slots_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    photo_url TEXT DEFAULT '' NOT NULL,
    bio TEXT NOT NULL DEFAULT 'Dedicated health professional focused on high-quality patient outcomes and modern diagnostics.',
    degree TEXT NOT NULL DEFAULT 'MBBS, MD',
    languages TEXT NOT NULL DEFAULT 'English, Hindi, Bengali',
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- ==========================================
-- 5. SCHEDULES TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS public.schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id UUID REFERENCES public.doctors(id) ON DELETE CASCADE NOT NULL,
    date_str DATE NOT NULL,
    from_time_str TEXT NOT NULL,
    to_time_str TEXT NOT NULL,
    max_patients INTEGER NOT NULL CHECK (max_patients > 0),
    is_open_for_booking BOOLEAN NOT NULL DEFAULT TRUE,
    is_holiday BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- ==========================================
-- 6. APPOINTMENTS (Bookings) TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS public.appointments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token_number INTEGER NOT NULL CHECK (token_number > 0),
    patient_name TEXT NOT NULL,
    patient_phone TEXT NOT NULL,
    age INTEGER NOT NULL CHECK (age >= 0),
    gender TEXT NOT NULL CHECK (gender IN ('Male', 'Female', 'Other')),
    doctor_id UUID REFERENCES public.doctors(id) ON DELETE CASCADE NOT NULL,
    date_str DATE NOT NULL,
    time_str TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'Upcoming' CHECK (status IN ('Upcoming', 'Completed', 'Cancelled')),
    payment_mode TEXT NOT NULL DEFAULT 'Pay at Pharmacy' CHECK (payment_mode IN ('Pay Online', 'Pay at Pharmacy')),
    payment_status TEXT NOT NULL DEFAULT 'Pending' CHECK (payment_status IN ('Pending', 'Paid')),
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- ==========================================
-- 7. HOLIDAYS TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS public.holidays (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id UUID REFERENCES public.doctors(id) ON DELETE CASCADE NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason TEXT NULL,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- ==========================================
-- 8. NOTIFICATIONS TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS public.notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE NULL, -- NULL means targeted to Master Admin or global broadcast
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- ==========================================
-- 9. AUTH SESSIONS (Single Device Lock)
-- ==========================================
CREATE TABLE IF NOT EXISTS public.auth_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pharmacy_id UUID REFERENCES public.pharmacies(id) ON DELETE CASCADE NOT NULL,
    device_id TEXT NOT NULL,
    login_time TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- ==========================================
-- 10. AUDIT LOGS TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS public.audit_logs (
    log_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES auth.users(id) ON DELETE SET NULL,
    role TEXT NOT NULL,
    action TEXT NOT NULL,
    table_name TEXT NOT NULL,
    record_id UUID NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- ==========================================
-- PERFORMANCE INDEXES
-- ==========================================
CREATE INDEX IF NOT EXISTS idx_profiles_email ON public.profiles(email);
CREATE INDEX IF NOT EXISTS idx_profiles_role ON public.profiles(role);
CREATE INDEX IF NOT EXISTS idx_profiles_pharmacy ON public.profiles(pharmacy_id);
CREATE INDEX IF NOT EXISTS idx_pharmacies_email ON public.pharmacies(email);
CREATE INDEX IF NOT EXISTS idx_doctors_pharmacy ON public.doctors(pharmacy_id);
CREATE INDEX IF NOT EXISTS idx_doctor_is_deleted ON public.doctors(is_deleted);
CREATE INDEX IF NOT EXISTS idx_schedules_doctor ON public.schedules(doctor_id, date_str);
CREATE INDEX IF NOT EXISTS idx_appointments_doctor ON public.appointments(doctor_id, date_str);
CREATE INDEX IF NOT EXISTS idx_appointments_patient_phone ON public.appointments(patient_phone);
CREATE INDEX IF NOT EXISTS idx_auth_sessions_active ON public.auth_sessions(pharmacy_id, is_active);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created ON public.audit_logs(created_at);
CREATE INDEX IF NOT EXISTS idx_notifications_user ON public.notifications(user_id, is_read);

-- ==========================================
-- DB FUNCTIONS AND AUTOMATED TRIGGERS
-- ==========================================

-- Trigger: Automatically handle updated_at timestamps
CREATE OR REPLACE FUNCTION public.update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_profiles_modtime BEFORE UPDATE ON public.profiles FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();
CREATE TRIGGER update_pharmacies_modtime BEFORE UPDATE ON public.pharmacies FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();
CREATE TRIGGER update_doctors_modtime BEFORE UPDATE ON public.doctors FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();
CREATE TRIGGER update_schedules_modtime BEFORE UPDATE ON public.schedules FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();
CREATE TRIGGER update_appointments_modtime BEFORE UPDATE ON public.appointments FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();

-- Trigger: After Auth Signup - automatically populate profile or master admin
CREATE OR REPLACE FUNCTION public.handle_new_auth_user()
RETURNS TRIGGER AS $$
DECLARE
    v_role TEXT := 'patient';
    v_status TEXT := 'active';
    v_pharmacy_id UUID := NULL;
    v_is_master BOOLEAN := FALSE;
BEGIN
    -- Explicitly verify if the registrant is the Master Admin
    IF NEW.email = 'soumyadeepsarkar92@gmail.com' THEN
        v_role := 'master_admin';
        v_status := 'active';
        v_is_master := TRUE;
    ELSE
        -- Check if there is an in-transit metadata parameter signaling back-end manual creation
        IF (NEW.raw_user_meta_data->>'role') = 'pharmacy' THEN
            v_role := 'pharmacy';
            v_status := 'active';
            v_pharmacy_id := (NEW.raw_user_meta_data->>'pharmacy_id')::UUID;
        ELSE
            v_role := 'patient';
            v_status := 'active';
        END IF;
    END IF;

    -- Upsert the profile safely
    INSERT INTO public.profiles (id, email, role, status, pharmacy_id, is_deleted, created_at, updated_at)
    VALUES (NEW.id, NEW.email, v_role, v_status, v_pharmacy_id, FALSE, NOW(), NOW())
    ON CONFLICT (id) DO UPDATE
    SET email = EXCLUDED.email,
        role = EXCLUDED.role,
        status = EXCLUDED.status,
        pharmacy_id = COALESCE(EXCLUDED.pharmacy_id, public.profiles.pharmacy_id),
        updated_at = NOW();

    -- Populate Master Admin catalog table
    IF v_is_master THEN
        INSERT INTO public.master_admins (id, email, created_at)
        VALUES (NEW.id, NEW.email, NOW())
        ON CONFLICT (id) DO NOTHING;
    END IF;

    -- Produce a Master Admin notice if a new account is registered
    IF v_role = 'pharmacy' THEN
        INSERT INTO public.notifications (user_id, title, message, is_read, created_at)
        VALUES (
            NULL, 
            'New Pharmacy Created', 
            'Pharmacy "' || COALESCE((SELECT name FROM public.pharmacies WHERE id = v_pharmacy_id), 'New Pharmacy') || '" has been registered & active.', 
            FALSE, 
            NOW()
        );
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Map to auth trigger
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_auth_user();

-- Trigger: Device restriction enforcement (Single Session Lock)
CREATE OR REPLACE FUNCTION public.enforce_single_active_session()
RETURNS TRIGGER AS $$
BEGIN
    -- If registering an active session, mark all previous sessions for this pharmacy account as inactive
    IF NEW.is_active = TRUE THEN
        UPDATE public.auth_sessions
        SET is_active = FALSE
        WHERE pharmacy_id = NEW.pharmacy_id AND id != NEW.id AND is_active = TRUE;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_enforce_session BEFORE INSERT ON public.auth_sessions
FOR EACH ROW EXECUTE FUNCTION public.enforce_single_active_session();

-- ==========================================
-- DAILY CRON JOB: SUBSCRIPTION EXPIRY SYSTEM
-- ==========================================
CREATE OR REPLACE FUNCTION public.check_expired_subscriptions()
RETURNS VOID AS $$
DECLARE
    r_pharmacy RECORD;
BEGIN
    -- 1. Identify all active pharmacies that have surpassed their subscription expiry date
    FOR r_pharmacy IN 
        SELECT id, name, subscription_expiry, status 
        FROM public.pharmacies
        WHERE subscription_expiry < CURRENT_DATE 
          AND status = 'active'
          AND is_deleted = FALSE
    LOOP
        -- A. Suspend SaaS core pharmacy account
        UPDATE public.pharmacies
        SET status = 'suspended',
            updated_at = NOW()
        WHERE id = r_pharmacy.id;

        -- B. Disable pharmacy login (suspend matching public login profiles)
        UPDATE public.profiles
        SET status = 'suspended',
            updated_at = NOW()
        WHERE pharmacy_id = r_pharmacy.id AND is_deleted = FALSE;

        -- C. Disable patient booking (mark associated doctor schedules as closed for booking)
        UPDATE public.schedules
        SET is_open_for_booking = false,
            updated_at = NOW()
        WHERE doctor_id IN (SELECT id FROM public.doctors WHERE pharmacy_id = r_pharmacy.id);

        -- D. Generate audit log of automatic suspension
        INSERT INTO public.audit_logs (user_id, role, action, table_name, record_id, created_at)
        VALUES (NULL, 'system', 'Subscription Validity Expired: Auto suspended login & disabled patient bookings', 'pharmacies', r_pharmacy.id, NOW());

        -- E. Insert notification for Master Admin (user_id = NULL)
        INSERT INTO public.notifications (user_id, title, message, is_read, created_at)
        VALUES (
            NULL, 
            'SaaS Subscription Expired', 
            'The subscription for "' || r_pharmacy.name || '" expired on ' || r_pharmacy.subscription_expiry::TEXT || '. The account login has been suspended and bookings disabled.', 
            FALSE, 
            NOW()
        );
    END LOOP;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Map daily cron execution of the SQL function
-- SELECT cron.schedule('check-expired-subscriptions-daily', '0 0 * * *', 'SELECT public.check_expired_subscriptions();');

-- Alternative Edge Function invocation via pg_net (Uncomment if using Edge Function cron scheduler)
-- SELECT cron.schedule('check-expired-subscriptions-edge-daily', '0 0 * * *', 'SELECT net.http_post(url := ''https://your-project-ref.supabase.co/functions/v1/check_expired_subscriptions'', headers := ''{"Content-Type": "application/json", "Authorization": "Bearer YOUR_SERVICE_ROLE_KEY"}''::jsonb);');


-- ==========================================
-- ROW-LEVEL SECURITY (RLS) DESIGN POLICIES
-- ==========================================

-- Enable RLS across all models
ALTER TABLE public.master_admins ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pharmacies ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.doctors ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.schedules ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.appointments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.holidays ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.auth_sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.audit_logs ENABLE ROW LEVEL SECURITY;

-- Helper security verification functions
CREATE OR REPLACE FUNCTION public.is_master_admin(user_id UUID)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM public.profiles 
        WHERE id = user_id AND (role = 'master_admin' OR email = 'soumyadeepsarkar92@gmail.com')
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE FUNCTION public.get_pharmacy_id(user_id UUID)
RETURNS UUID AS $$
DECLARE
    v_pharm_id UUID;
BEGIN
    SELECT pharmacy_id INTO v_pharm_id FROM public.profiles WHERE id = user_id;
    RETURN v_pharm_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ---------------- profiles policies ----------------
CREATE POLICY "Master admins have full visibility and write privileges"
ON public.profiles
FOR ALL
USING (public.is_master_admin(auth.uid()))
WITH CHECK (public.is_master_admin(auth.uid()));

CREATE POLICY "Users can query and update their own profiles"
ON public.profiles
FOR ALL
USING (auth.uid() = id)
WITH CHECK (auth.uid() = id);

-- ---------------- pharmacies policies ----------------
CREATE POLICY "Master admins can manage pharmacies completely"
ON public.pharmacies
FOR ALL
USING (public.is_master_admin(auth.uid()))
WITH CHECK (public.is_master_admin(auth.uid()));

CREATE POLICY "Pharmacies can view their own company records"
ON public.pharmacies
FOR SELECT
USING (id = public.get_pharmacy_id(auth.uid()));

-- ---------------- doctors policies ----------------
CREATE POLICY "Master admin can view or select all doctors"
ON public.doctors
FOR SELECT
USING (public.is_master_admin(auth.uid()));

CREATE POLICY "Pharmacies can read/write their own doctors"
ON public.doctors
FOR ALL
USING (pharmacy_id = public.get_pharmacy_id(auth.uid()))
WITH CHECK (pharmacy_id = public.get_pharmacy_id(auth.uid()));

CREATE POLICY "Patients can view all enabled active doctors"
ON public.doctors
FOR SELECT
USING (is_enabled = TRUE AND is_deleted = FALSE);

-- ---------------- schedules policies ----------------
CREATE POLICY "Master admin can read schedules"
ON public.schedules
FOR SELECT
USING (public.is_master_admin(auth.uid()));

CREATE POLICY "Pharmacies can manage schedules for their own doctors"
ON public.schedules
FOR ALL
USING (
    doctor_id IN (
        SELECT id FROM public.doctors 
        WHERE pharmacy_id = public.get_pharmacy_id(auth.uid())
    )
)
WITH CHECK (
    doctor_id IN (
        SELECT id FROM public.doctors 
        WHERE pharmacy_id = public.get_pharmacy_id(auth.uid())
    )
);

CREATE POLICY "Patients can select and query open active schedules"
ON public.schedules
FOR SELECT
USING (is_open_for_booking = TRUE AND is_deleted = FALSE);

-- ---------------- appointments / bookings policies ----------------
CREATE POLICY "Master admin can manage all appointments"
ON public.appointments
FOR ALL
USING (public.is_master_admin(auth.uid()))
WITH CHECK (public.is_master_admin(auth.uid()));

CREATE POLICY "Pharmacies can process bookings for their own doctors"
ON public.appointments
FOR ALL
USING (
    doctor_id IN (
        SELECT id FROM public.doctors 
        WHERE pharmacy_id = public.get_pharmacy_id(auth.uid())
    )
)
WITH CHECK (
    doctor_id IN (
        SELECT id FROM public.doctors 
        WHERE pharmacy_id = public.get_pharmacy_id(auth.uid())
    )
);

CREATE POLICY "Patients can manage their own appointments"
ON public.appointments
FOR ALL
USING (
    patient_phone = (
        SELECT phone FROM public.profiles WHERE id = auth.uid()
    ) OR EXISTS (
        -- Or direct auth session link or guest/oauth state matches
        SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'patient'
    )
)
WITH CHECK (
    patient_phone = (
        SELECT phone FROM public.profiles WHERE id = auth.uid()
    )
);

-- ---------------- notifications policies ----------------
CREATE POLICY "Master admins can read and write all notifications"
ON public.notifications
FOR ALL
USING (public.is_master_admin(auth.uid()))
WITH CHECK (public.is_master_admin(auth.uid()));

CREATE POLICY "Users can access their own direct notifications"
ON public.notifications
FOR ALL
USING (user_id = auth.uid())
WITH CHECK (user_id = auth.uid());

-- ---------------- audit_logs policies ----------------
CREATE POLICY "Only master admins can access audit trail"
ON public.audit_logs
FOR SELECT
USING (public.is_master_admin(auth.uid()));

CREATE POLICY "Active system triggers can populate audit reports"
ON public.audit_logs
FOR INSERT
WITH CHECK (TRUE);

-- ---------------- auth_sessions policies ----------------
CREATE POLICY "Master admin can monitor sessions list"
ON public.auth_sessions
FOR SELECT
USING (public.is_master_admin(auth.uid()));

CREATE POLICY "Pharmacies can manage their own session logs"
ON public.auth_sessions
FOR ALL
USING (pharmacy_id = public.get_pharmacy_id(auth.uid()))
WITH CHECK (pharmacy_id = public.get_pharmacy_id(auth.uid()));
