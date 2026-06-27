-- Migration to add Doctor role and missing tables, and implement RLS policies for all 4 roles

-- 1. Update profiles for doctor role
ALTER TABLE public.profiles DROP CONSTRAINT IF EXISTS profiles_role_check;
ALTER TABLE public.profiles ADD CONSTRAINT profiles_role_check CHECK (role IN ('master_admin', 'pharmacy', 'patient', 'doctor'));
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS doctor_id UUID REFERENCES public.doctors(id) ON DELETE SET NULL;

-- Helper function to get doctor_id
CREATE OR REPLACE FUNCTION public.get_doctor_id(user_id UUID)
RETURNS UUID AS $$
DECLARE
    v_doc_id UUID;
BEGIN
    SELECT doctor_id INTO v_doc_id FROM public.profiles WHERE id = user_id;
    RETURN v_doc_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 2. Create missing tables to sync with Room DB
CREATE TABLE IF NOT EXISTS public.subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pharmacy_id UUID REFERENCES public.pharmacies(id) ON DELETE CASCADE,
    current_plan TEXT NOT NULL DEFAULT 'Standard Monthly Plan',
    price DECIMAL(10,2) NOT NULL DEFAULT 299.0,
    validity_date TEXT NOT NULL,
    auto_renewal BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID REFERENCES public.appointments(id) ON DELETE CASCADE,
    amount DECIMAL(10,2) NOT NULL,
    status TEXT NOT NULL,
    date_str TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    patient_name TEXT NOT NULL,
    doctor_id UUID REFERENCES public.doctors(id) ON DELETE CASCADE,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    review TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.pharmacy_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pharmacy_name TEXT NOT NULL,
    owner_name TEXT NOT NULL,
    license_no TEXT UNIQUE NOT NULL,
    mobile TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    address TEXT NOT NULL,
    license_image TEXT NOT NULL,
    pharmacy_photo TEXT,
    status TEXT NOT NULL DEFAULT 'pending',
    approved_at TIMESTAMP WITH TIME ZONE NULL,
    approved_by TEXT NULL,
    payment_id TEXT NULL,
    payment_status TEXT NULL,
    payment_amount DECIMAL(10,2) NULL,
    payment_date TIMESTAMP WITH TIME ZONE NULL,
    rejection_reason TEXT NULL,
    correction_notes TEXT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.favourite_doctors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    doctor_id UUID REFERENCES public.doctors(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_favourite_doctors_unique ON public.favourite_doctors(patient_id, doctor_id);

CREATE TABLE IF NOT EXISTS public.pricing_settings (
    id TEXT PRIMARY KEY DEFAULT 'default_pricing',
    registration_fee DECIMAL(10,2) NOT NULL DEFAULT 10.0,
    monthly_subscription_fee DECIMAL(10,2) NOT NULL DEFAULT 10.0,
    quarterly_subscription_fee DECIMAL(10,2) NOT NULL DEFAULT 30.0,
    yearly_subscription_fee DECIMAL(10,2) NOT NULL DEFAULT 100.0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.payment_history (
    payment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id TEXT NOT NULL UNIQUE,
    amount DECIMAL(10,2) NOT NULL,
    status TEXT NOT NULL,
    type TEXT NOT NULL,
    failure_reason TEXT NULL,
    pharmacy_id UUID REFERENCES public.pharmacies(id) ON DELETE SET NULL,
    signature TEXT NULL,
    method TEXT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- Triggers for new tables
CREATE TRIGGER update_subscriptions_modtime BEFORE UPDATE ON public.subscriptions FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();
CREATE TRIGGER update_payments_modtime BEFORE UPDATE ON public.payments FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();
CREATE TRIGGER update_reviews_modtime BEFORE UPDATE ON public.reviews FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();
CREATE TRIGGER update_pharmacy_requests_modtime BEFORE UPDATE ON public.pharmacy_requests FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();
CREATE TRIGGER update_pricing_settings_modtime BEFORE UPDATE ON public.pricing_settings FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();
CREATE TRIGGER update_payment_history_modtime BEFORE UPDATE ON public.payment_history FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();


-- 3. Enable RLS on new tables
ALTER TABLE public.subscriptions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.reviews ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pharmacy_requests ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.favourite_doctors ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pricing_settings ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.payment_history ENABLE ROW LEVEL SECURITY;

-- 4. RLS POLICIES FOR: PATIENTS, DOCTORS, PHARMACIES, ADMINS

-- (A) Admins (already have full access on most, let's add for new tables)
CREATE POLICY "Admins have full access to subscriptions" ON public.subscriptions FOR ALL USING (public.is_master_admin(auth.uid())) WITH CHECK (public.is_master_admin(auth.uid()));
CREATE POLICY "Admins have full access to payments" ON public.payments FOR ALL USING (public.is_master_admin(auth.uid())) WITH CHECK (public.is_master_admin(auth.uid()));
CREATE POLICY "Admins have full access to reviews" ON public.reviews FOR ALL USING (public.is_master_admin(auth.uid())) WITH CHECK (public.is_master_admin(auth.uid()));
CREATE POLICY "Admins have full access to pharmacy_requests" ON public.pharmacy_requests FOR ALL USING (public.is_master_admin(auth.uid())) WITH CHECK (public.is_master_admin(auth.uid()));
CREATE POLICY "Admins have full access to favourite_doctors" ON public.favourite_doctors FOR ALL USING (public.is_master_admin(auth.uid())) WITH CHECK (public.is_master_admin(auth.uid()));
CREATE POLICY "Admins have full access to pricing_settings" ON public.pricing_settings FOR ALL USING (public.is_master_admin(auth.uid())) WITH CHECK (public.is_master_admin(auth.uid()));
CREATE POLICY "Admins have full access to payment_history" ON public.payment_history FOR ALL USING (public.is_master_admin(auth.uid())) WITH CHECK (public.is_master_admin(auth.uid()));

-- (B) Pharmacies
CREATE POLICY "Pharmacies can view their subscriptions" ON public.subscriptions FOR SELECT USING (pharmacy_id = public.get_pharmacy_id(auth.uid()));
CREATE POLICY "Pharmacies can manage their own payment history" ON public.payment_history FOR ALL USING (pharmacy_id = public.get_pharmacy_id(auth.uid())) WITH CHECK (pharmacy_id = public.get_pharmacy_id(auth.uid()));
CREATE POLICY "Pharmacies can see reviews of their doctors" ON public.reviews FOR SELECT USING (doctor_id IN (SELECT id FROM public.doctors WHERE pharmacy_id = public.get_pharmacy_id(auth.uid())));

-- (C) Patients
CREATE POLICY "Patients can view pricing settings" ON public.pricing_settings FOR SELECT USING (TRUE);
CREATE POLICY "Patients can manage their own favourite doctors" ON public.favourite_doctors FOR ALL USING (patient_id = auth.uid()) WITH CHECK (patient_id = auth.uid());
CREATE POLICY "Patients can write and view reviews" ON public.reviews FOR INSERT WITH CHECK (patient_id = auth.uid());
CREATE POLICY "Anyone can view reviews" ON public.reviews FOR SELECT USING (TRUE);
CREATE POLICY "Patients can see their own payments" ON public.payments FOR SELECT USING (booking_id IN (SELECT id FROM public.appointments WHERE patient_phone = (SELECT phone FROM public.profiles WHERE id = auth.uid())));
CREATE POLICY "Anyone can insert pharmacy_requests" ON public.pharmacy_requests FOR INSERT WITH CHECK (TRUE);

-- (D) Doctors (New Role Policies for existing and new tables)
-- Allow doctors to read their own profile
CREATE POLICY "Doctors can read their own doctor record" ON public.doctors FOR SELECT USING (id = public.get_doctor_id(auth.uid()));
CREATE POLICY "Doctors can manage their own schedules" ON public.schedules FOR ALL USING (doctor_id = public.get_doctor_id(auth.uid())) WITH CHECK (doctor_id = public.get_doctor_id(auth.uid()));
CREATE POLICY "Doctors can manage their own appointments" ON public.appointments FOR ALL USING (doctor_id = public.get_doctor_id(auth.uid())) WITH CHECK (doctor_id = public.get_doctor_id(auth.uid()));
CREATE POLICY "Doctors can view their own reviews" ON public.reviews FOR SELECT USING (doctor_id = public.get_doctor_id(auth.uid()));
CREATE POLICY "Doctors can manage their own holidays" ON public.holidays FOR ALL USING (doctor_id = public.get_doctor_id(auth.uid())) WITH CHECK (doctor_id = public.get_doctor_id(auth.uid()));

