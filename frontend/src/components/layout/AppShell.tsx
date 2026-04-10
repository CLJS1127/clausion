import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';

interface AppShellProps {
  role: 'student' | 'instructor';
}

export default function AppShell({ role }: AppShellProps) {
  return (
    <div className="flex h-screen bg-slate-50 overflow-hidden">
      <Sidebar role={role} />
      <main className="flex-1 overflow-y-auto">
        <div className="p-6 max-w-7xl mx-auto">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
