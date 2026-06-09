import { Toaster } from '@/components/ui/sonner';
import { TooltipProvider } from '@/components/ui/tooltip';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Provider } from 'react-redux';
import { store } from '@/store';
import Layout from './components/Layout';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Horses from './pages/Horses';
import Jockeys from './pages/Jockeys';
import Schedules from './pages/Schedules';
import Results from './pages/Results';
import ProtectedRoute from './components/ProtectedRoute';

const App = () => (
  <Provider store={store}>
    <TooltipProvider>
      <Toaster />
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/" element={<ProtectedRoute><Layout /></ProtectedRoute>}>
            <Route index element={<Dashboard />} />
            <Route path="horses" element={<Horses />} />
            <Route path="jockeys" element={<Jockeys />} />
            <Route path="schedules" element={<Schedules />} />
            <Route path="results" element={<Results />} />
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </TooltipProvider>
  </Provider>
);

export default App;