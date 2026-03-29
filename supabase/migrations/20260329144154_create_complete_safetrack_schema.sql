/*
  # SafeTrack Complete Database Schema

  1. Tables Created
    - `users` - User accounts with roles, levels, violations
    - `incidents` - Incident reports with status and flagging
    - `incident_upvotes` - Track one-vote-per-user
    - `incident_reports` - User reports on posts (moderation)
    - `user_violations` - Warning/strike tracking
    - `archived_incidents` - Soft-deleted incidents
    - `user_badges` - Achievement system
    - `notifications` - User notifications

  2. Security
    - RLS enabled on all tables
    - Policies for user/admin access control
*/

-- Users table
CREATE TABLE IF NOT EXISTS users (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  username text UNIQUE NOT NULL,
  mobile text NOT NULL,
  password text NOT NULL,
  role text DEFAULT 'user',
  level text DEFAULT 'Observer',
  report_count int DEFAULT 0,
  violation_count int DEFAULT 0,
  is_blocked boolean DEFAULT false,
  is_banned boolean DEFAULT false,
  flag_count int DEFAULT 0,
  created_at timestamptz DEFAULT now()
);

ALTER TABLE users ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view all users"
  ON users FOR SELECT
  TO authenticated
  USING (true);

CREATE POLICY "Users can insert themselves"
  ON users FOR INSERT
  TO authenticated
  WITH CHECK (true);

CREATE POLICY "Users can update themselves"
  ON users FOR UPDATE
  TO authenticated
  USING (id = auth.uid())
  WITH CHECK (id = auth.uid());

CREATE POLICY "Admins can update any user"
  ON users FOR UPDATE
  TO authenticated
  USING (
    EXISTS (
      SELECT 1 FROM users u
      WHERE u.id = auth.uid()
      AND u.role = 'admin'
    )
  )
  WITH CHECK (
    EXISTS (
      SELECT 1 FROM users u
      WHERE u.id = auth.uid()
      AND u.role = 'admin'
    )
  );

-- Incidents table
CREATE TABLE IF NOT EXISTS incidents (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  title text NOT NULL,
  type text NOT NULL,
  description text NOT NULL,
  location text NOT NULL,
  status text DEFAULT 'Pending',
  upvotes int DEFAULT 0,
  image_base64 text DEFAULT '',
  reporter_id text NOT NULL,
  reporter_name text NOT NULL,
  is_anonymous boolean DEFAULT false,
  flag_count int DEFAULT 0,
  is_flagged boolean DEFAULT false,
  archived boolean DEFAULT false,
  created_at timestamptz DEFAULT now()
);

ALTER TABLE incidents ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Anyone can view non-archived incidents"
  ON incidents FOR SELECT
  TO authenticated
  USING (archived = false);

CREATE POLICY "Users can insert incidents"
  ON incidents FOR INSERT
  TO authenticated
  WITH CHECK (true);

CREATE POLICY "Users can update own incidents"
  ON incidents FOR UPDATE
  TO authenticated
  USING (reporter_id = auth.uid()::text)
  WITH CHECK (reporter_id = auth.uid()::text);

CREATE POLICY "Admins can update any incident"
  ON incidents FOR UPDATE
  TO authenticated
  USING (
    EXISTS (
      SELECT 1 FROM users
      WHERE users.id = auth.uid()
      AND users.role = 'admin'
    )
  )
  WITH CHECK (
    EXISTS (
      SELECT 1 FROM users
      WHERE users.id = auth.uid()
      AND users.role = 'admin'
    )
  );

CREATE POLICY "Users can delete own incidents"
  ON incidents FOR DELETE
  TO authenticated
  USING (reporter_id = auth.uid()::text);

CREATE POLICY "Admins can delete any incident"
  ON incidents FOR DELETE
  TO authenticated
  USING (
    EXISTS (
      SELECT 1 FROM users
      WHERE users.id = auth.uid()
      AND users.role = 'admin'
    )
  );

-- Incident upvotes table
CREATE TABLE IF NOT EXISTS incident_upvotes (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  incident_id text NOT NULL,
  user_id text NOT NULL,
  created_at timestamptz DEFAULT now(),
  UNIQUE(incident_id, user_id)
);

ALTER TABLE incident_upvotes ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can insert their upvotes"
  ON incident_upvotes FOR INSERT
  TO authenticated
  WITH CHECK (user_id = auth.uid()::text);

CREATE POLICY "Users can view all upvotes"
  ON incident_upvotes FOR SELECT
  TO authenticated
  USING (true);

-- Incident reports table (user reporting system)
CREATE TABLE IF NOT EXISTS incident_reports (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  incident_id text NOT NULL,
  reporter_id text NOT NULL,
  reported_user_id text NOT NULL,
  reason text NOT NULL,
  proof_text text DEFAULT '',
  is_valid boolean DEFAULT NULL,
  admin_note text DEFAULT '',
  violation_applied boolean DEFAULT false,
  created_at timestamptz DEFAULT now()
);

ALTER TABLE incident_reports ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can insert reports"
  ON incident_reports FOR INSERT
  TO authenticated
  WITH CHECK (reporter_id = auth.uid()::text);

CREATE POLICY "Users can view own reports"
  ON incident_reports FOR SELECT
  TO authenticated
  USING (reporter_id = auth.uid()::text);

CREATE POLICY "Admins can view all reports"
  ON incident_reports FOR SELECT
  TO authenticated
  USING (
    EXISTS (
      SELECT 1 FROM users
      WHERE users.id = auth.uid()
      AND users.role = 'admin'
    )
  );

CREATE POLICY "Admins can update reports"
  ON incident_reports FOR UPDATE
  TO authenticated
  USING (
    EXISTS (
      SELECT 1 FROM users
      WHERE users.id = auth.uid()
      AND users.role = 'admin'
    )
  )
  WITH CHECK (
    EXISTS (
      SELECT 1 FROM users
      WHERE users.id = auth.uid()
      AND users.role = 'admin'
    )
  );

-- User violations table
CREATE TABLE IF NOT EXISTS user_violations (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id text NOT NULL,
  reason text NOT NULL,
  strike_number int NOT NULL,
  given_by text NOT NULL,
  created_at timestamptz DEFAULT now()
);

ALTER TABLE user_violations ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own violations"
  ON user_violations FOR SELECT
  TO authenticated
  USING (user_id = auth.uid()::text);

CREATE POLICY "Admins can insert violations"
  ON user_violations FOR INSERT
  TO authenticated
  WITH CHECK (
    EXISTS (
      SELECT 1 FROM users
      WHERE users.id = auth.uid()
      AND users.role = 'admin'
    )
  );

CREATE POLICY "Admins can view all violations"
  ON user_violations FOR SELECT
  TO authenticated
  USING (
    EXISTS (
      SELECT 1 FROM users
      WHERE users.id = auth.uid()
      AND users.role = 'admin'
    )
  );

-- Archived incidents table
CREATE TABLE IF NOT EXISTS archived_incidents (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  original_id text NOT NULL,
  title text NOT NULL,
  type text NOT NULL,
  description text NOT NULL,
  location text NOT NULL,
  status text DEFAULT 'Pending',
  upvotes int DEFAULT 0,
  image_base64 text DEFAULT '',
  reporter_id text NOT NULL,
  reporter_name text NOT NULL,
  is_anonymous boolean DEFAULT false,
  deleted_by text NOT NULL,
  deleted_at timestamptz DEFAULT now(),
  original_created_at timestamptz DEFAULT now()
);

ALTER TABLE archived_incidents ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Admins can insert archived"
  ON archived_incidents FOR INSERT
  TO authenticated
  WITH CHECK (
    EXISTS (
      SELECT 1 FROM users
      WHERE users.id = auth.uid()
      AND users.role = 'admin'
    )
  );

CREATE POLICY "Admins can view archived"
  ON archived_incidents FOR SELECT
  TO authenticated
  USING (
    EXISTS (
      SELECT 1 FROM users
      WHERE users.id = auth.uid()
      AND users.role = 'admin'
    )
  );

-- User badges table
CREATE TABLE IF NOT EXISTS user_badges (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id text NOT NULL,
  badge_name text NOT NULL,
  earned_at timestamptz DEFAULT now(),
  UNIQUE(user_id, badge_name)
);

ALTER TABLE user_badges ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own badges"
  ON user_badges FOR SELECT
  TO authenticated
  USING (user_id = auth.uid()::text);

CREATE POLICY "Anyone can view badges"
  ON user_badges FOR SELECT
  TO authenticated
  USING (true);

CREATE POLICY "System can insert badges"
  ON user_badges FOR INSERT
  TO authenticated
  WITH CHECK (true);

-- Notifications table
CREATE TABLE IF NOT EXISTS notifications (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id text NOT NULL,
  message text NOT NULL,
  type text DEFAULT 'info',
  is_read boolean DEFAULT false,
  created_at timestamptz DEFAULT now()
);

ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own notifications"
  ON notifications FOR SELECT
  TO authenticated
  USING (user_id = auth.uid()::text);

CREATE POLICY "System can insert notifications"
  ON notifications FOR INSERT
  TO authenticated
  WITH CHECK (true);

CREATE POLICY "Users can update own notifications"
  ON notifications FOR UPDATE
  TO authenticated
  USING (user_id = auth.uid()::text)
  WITH CHECK (user_id = auth.uid()::text);