import { BrowserRouter, Routes, Route } from 'react-router-dom';
import ErrorBoundary from './components/ErrorBoundary';
import DashboardLayout from './layouts/DashboardLayout';
import AiChatLayout from './pages/AiChatLayout';

export default function App() {
  return (
    <ErrorBoundary>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<DashboardLayout />} />
          <Route path="/ai-chat" element={<AiChatLayout />} />
          <Route path="/ai-chat/:sessionId" element={<AiChatLayout />} />
        </Routes>
      </BrowserRouter>
    </ErrorBoundary>
  );
}
