import { createBrowserRouter } from 'react-router-dom';
import { RootLayout } from '@/layouts/root-layout';
import { DashboardPage } from '@/pages/dashboard';
import { CreateSchemaPage } from '@/pages/create-schema/create-schema-page';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <RootLayout />,
    children: [
      { index: true, element: <DashboardPage /> },
      { path: 'schemas/new', element: <CreateSchemaPage /> },
    ],
  },
]);
