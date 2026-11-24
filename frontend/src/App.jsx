import React, { Suspense, lazy } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Layout from './components/Layout';
import { Loader } from './components/common/Loader';

// Code-split pages for faster initial bundle and instant page transitions
const Login = lazy(() => import('./pages/Login'));
const Register = lazy(() => import('./pages/Register'));
const Dashboard = lazy(() => import('./pages/Dashboard'));
const Medicines = lazy(() => import('./pages/Medicines'));
const Categories = lazy(() => import('./pages/Categories'));
const Suppliers = lazy(() => import('./pages/Suppliers'));
const StockMovements = lazy(() => import('./pages/StockMovements'));
const PurchaseOrders = lazy(() => import('./pages/PurchaseOrders'));
const Users = lazy(() => import('./pages/Users'));
const Billing = lazy(() => import('./pages/Billing'));
const SalesHistory = lazy(() => import('./pages/SalesHistory'));
const SupplierDashboard = lazy(() => import('./pages/supplier/SupplierDashboard'));
const SupplierProfile = lazy(() => import('./pages/supplier/SupplierProfile'));
const Profile = lazy(() => import('./pages/Profile'));
const Notifications = lazy(() => import('./pages/Notifications'));
const Messages = lazy(() => import('./pages/Messages'));
const Expiry = lazy(() => import('./pages/Expiry'));
const Reports = lazy(() => import('./pages/Reports'));

function App() {
  return (
    <Router>
      <AuthProvider>
        <Suspense fallback={<Loader message="Loading..." />}>
          <Routes>
          {/* Public routes */}
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* Authenticated routes */}
          <Route element={<ProtectedRoute />}>
            <Route element={<Layout />}>
              {/* Dashboard accessible by all roles */}
              <Route path="/" element={<Dashboard />} />
              <Route path="/messages" element={<Messages />} />
              <Route path="/expiry" element={<Expiry />} />
              <Route path="/reports" element={<Reports />} />

              {/* Supplier Dashboard accessible by Supplier */}
              <Route element={<ProtectedRoute allowedRoles={['ROLE_SUPPLIER']} />}>
                <Route path="/supplier/dashboard" element={<SupplierDashboard />} />
                <Route path="/supplier/profile" element={<SupplierProfile />} />
              </Route>

              {/* Profile accessible by all authenticated roles */}
              <Route path="/profile" element={<Profile />} />

              {/* Notifications accessible by Admin, Pharmacist, Staff, and Supplier */}
              <Route element={<ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_PHARMACIST', 'ROLE_STAFF', 'ROLE_SUPPLIER']} />}>
                <Route path="/notifications" element={<Notifications />} />
              </Route>

              {/* Medicines catalog accessible by all roles */}
              <Route path="/medicines" element={<Medicines />} />

              {/* Category accessible by Admin, Pharmacist, Staff, and Supplier */}
              <Route element={<ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_PHARMACIST', 'ROLE_STAFF', 'ROLE_SUPPLIER']} />}>
                <Route path="/categories" element={<Categories />} />
              </Route>

              {/* Billing and Sales accessible by Admin, Pharmacist, and Staff */}
              <Route element={<ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_PHARMACIST', 'ROLE_STAFF']} />}>
                <Route path="/billing" element={<Billing />} />
                <Route path="/sales" element={<SalesHistory />} />
              </Route>

              {/* Suppliers accessible by Admin, Pharmacist */}
              <Route element={<ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_PHARMACIST']} />}>
                <Route path="/suppliers" element={<Suppliers />} />
              </Route>


              {/* Stock Movements accessible by Admin, Pharmacist, and Staff */}
              <Route element={<ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_PHARMACIST', 'ROLE_STAFF']} />}>
                <Route path="/stock-movements" element={<StockMovements />} />
              </Route>

              {/* Purchase Orders accessible by Admin, Pharmacist, and Supplier */}
              <Route element={<ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_PHARMACIST', 'ROLE_SUPPLIER']} />}>
                <Route path="/purchase-orders" element={<PurchaseOrders />} />
              </Route>

              {/* Users Control only accessible by Admin */}
              <Route element={<ProtectedRoute allowedRoles={['ROLE_ADMIN']} />}>
                <Route path="/users" element={<Users />} />
              </Route>
            </Route>
          </Route>

          {/* Catch-all redirect */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
        </Suspense>
      </AuthProvider>
    </Router>
  );
}

export default App;
