import { createClient } from '@supabase/supabase-js';

// Get these from your Supabase Project Settings > API
const supabaseUrl = 'https://ydobyxgnnxpjyuuzzqod.supabase.co';
const supabaseAnonKey = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inlkb2J5eGdubnhwanl1dXp6cW9kIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzMwMTM2NzYsImV4cCI6MjA4ODU4OTY3Nn0.dzYZWCPCpKLnfD0J_J7pj4qeU26_wLpxfG1c3PpS4fY';

export const supabase = createClient(supabaseUrl, supabaseAnonKey);