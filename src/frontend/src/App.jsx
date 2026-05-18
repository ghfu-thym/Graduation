import React, { useEffect, useState } from 'react';
import Header from './components/Header';
import CreateEventForm from './pages/CreateEventForm';
import Vwr from './pages/VWR';

const getRouteFromHash = () => {
  const hash = window.location.hash.replace('#', '');
  if (hash.startsWith('/')) {
    return hash.slice(1);
  }
  return hash || 'vwr';
};

function App() {
  const [route, setRoute] = useState(getRouteFromHash());

  useEffect(() => {
    const handleHashChange = () => setRoute(getRouteFromHash());
    window.addEventListener('hashchange', handleHashChange);
    return () => window.removeEventListener('hashchange', handleHashChange);
  }, []);

  const isVwr = route === 'vwr';

  return (
    <div className="bg-background min-h-screen flex flex-col">
      <Header />
      {isVwr ? <Vwr /> : <CreateEventForm />}
    </div>
  );
}

export default App;
