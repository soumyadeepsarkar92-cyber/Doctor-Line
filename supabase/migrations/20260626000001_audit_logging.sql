-- Appointment History, Audit, Notifications
CREATE TABLE IF NOT EXISTS public.appointment_audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID REFERENCES public.appointments(id) ON DELETE CASCADE,
    action TEXT NOT NULL,
    details TEXT NOT NULL,
    timestamp BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.appointment_notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID REFERENCES public.appointments(id) ON DELETE CASCADE,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    timestamp BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.appointment_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID REFERENCES public.appointments(id) ON DELETE CASCADE,
    status TEXT NOT NULL,
    timestamp BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- Payment History, Audit, Notifications
CREATE TABLE IF NOT EXISTS public.payment_audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID REFERENCES public.payments(id) ON DELETE CASCADE,
    action TEXT NOT NULL,
    details TEXT NOT NULL,
    timestamp BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.payment_notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID REFERENCES public.payments(id) ON DELETE CASCADE,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    timestamp BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.payment_tracking_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID REFERENCES public.payments(id) ON DELETE CASCADE,
    status TEXT NOT NULL,
    timestamp BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- Pharmacy Approvals History, Audit, Notifications
CREATE TABLE IF NOT EXISTS public.pharmacy_approval_audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    request_id UUID REFERENCES public.pharmacy_requests(id) ON DELETE CASCADE,
    action TEXT NOT NULL,
    details TEXT NOT NULL,
    timestamp BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.pharmacy_approval_notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    request_id UUID REFERENCES public.pharmacy_requests(id) ON DELETE CASCADE,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    timestamp BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.pharmacy_approval_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    request_id UUID REFERENCES public.pharmacy_requests(id) ON DELETE CASCADE,
    status TEXT NOT NULL,
    notes TEXT NOT NULL,
    timestamp BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- Triggers for modtime
CREATE TRIGGER update_appointment_audit_logs_modtime BEFORE UPDATE ON public.appointment_audit_logs FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();
CREATE TRIGGER update_appointment_notifications_modtime BEFORE UPDATE ON public.appointment_notifications FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();
CREATE TRIGGER update_appointment_history_modtime BEFORE UPDATE ON public.appointment_history FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();

CREATE TRIGGER update_payment_audit_logs_modtime BEFORE UPDATE ON public.payment_audit_logs FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();
CREATE TRIGGER update_payment_notifications_modtime BEFORE UPDATE ON public.payment_notifications FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();
CREATE TRIGGER update_payment_tracking_history_modtime BEFORE UPDATE ON public.payment_tracking_history FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();

CREATE TRIGGER update_pharmacy_approval_audit_logs_modtime BEFORE UPDATE ON public.pharmacy_approval_audit_logs FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();
CREATE TRIGGER update_pharmacy_approval_notifications_modtime BEFORE UPDATE ON public.pharmacy_approval_notifications FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();
CREATE TRIGGER update_pharmacy_approval_history_modtime BEFORE UPDATE ON public.pharmacy_approval_history FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();


-- RLS
ALTER TABLE public.appointment_audit_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.appointment_notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.appointment_history ENABLE ROW LEVEL SECURITY;

ALTER TABLE public.payment_audit_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.payment_notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.payment_tracking_history ENABLE ROW LEVEL SECURITY;

ALTER TABLE public.pharmacy_approval_audit_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pharmacy_approval_notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pharmacy_approval_history ENABLE ROW LEVEL SECURITY;

-- Admins can view/manage all these
CREATE POLICY "Admins have full access to appointment logs" ON public.appointment_audit_logs FOR ALL USING (public.is_master_admin(auth.uid())) WITH CHECK (public.is_master_admin(auth.uid()));
CREATE POLICY "Admins have full access to appointment notifications" ON public.appointment_notifications FOR ALL USING (public.is_master_admin(auth.uid())) WITH CHECK (public.is_master_admin(auth.uid()));
CREATE POLICY "Admins have full access to appointment history" ON public.appointment_history FOR ALL USING (public.is_master_admin(auth.uid())) WITH CHECK (public.is_master_admin(auth.uid()));

CREATE POLICY "Admins have full access to payment logs" ON public.payment_audit_logs FOR ALL USING (public.is_master_admin(auth.uid())) WITH CHECK (public.is_master_admin(auth.uid()));
CREATE POLICY "Admins have full access to payment notifications" ON public.payment_notifications FOR ALL USING (public.is_master_admin(auth.uid())) WITH CHECK (public.is_master_admin(auth.uid()));
CREATE POLICY "Admins have full access to payment tracking history" ON public.payment_tracking_history FOR ALL USING (public.is_master_admin(auth.uid())) WITH CHECK (public.is_master_admin(auth.uid()));

CREATE POLICY "Admins have full access to pharmacy approval logs" ON public.pharmacy_approval_audit_logs FOR ALL USING (public.is_master_admin(auth.uid())) WITH CHECK (public.is_master_admin(auth.uid()));
CREATE POLICY "Admins have full access to pharmacy approval notifications" ON public.pharmacy_approval_notifications FOR ALL USING (public.is_master_admin(auth.uid())) WITH CHECK (public.is_master_admin(auth.uid()));
CREATE POLICY "Admins have full access to pharmacy approval history" ON public.pharmacy_approval_history FOR ALL USING (public.is_master_admin(auth.uid())) WITH CHECK (public.is_master_admin(auth.uid()));
