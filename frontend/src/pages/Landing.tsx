import AnimatedGradientHero from '../components/landing/AnimatedGradientHero';
import HowItWorksSection from '../components/landing/HowItWorksSection';
import RoleValueCards from '../components/landing/RoleValueCards';
import CTASection from '../components/landing/CTASection';

export default function Landing() {
  return (
    <div className="min-h-screen">
      <AnimatedGradientHero />
      <HowItWorksSection />
      <RoleValueCards />
      <CTASection />

      {/* Footer */}
      <footer className="bg-slate-900 text-slate-400 py-12 px-6">
        <div className="max-w-6xl mx-auto flex flex-col md:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-2">
            <div className="w-7 h-7 rounded-lg bg-gradient-to-br from-indigo-600 to-violet-600 flex items-center justify-center">
              <span className="text-white text-xs font-bold">CP</span>
            </div>
            <span className="text-sm font-bold text-white">ClassPulse Twin</span>
          </div>
          <p className="text-xs text-slate-500">
            &copy; 2026 ClassPulse Twin. AI-powered education intervention platform.
          </p>
        </div>
      </footer>
    </div>
  );
}
