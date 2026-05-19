import React, { useEffect, useState } from 'react';
import Header from './components/Header';
import CreateEventForm from './pages/CreateEventForm';
import Vwr from './pages/VWR';
import Home from './pages/Home';

const getRouteFromHash = () => {
  const hash = window.location.hash.replace('#', '');
  if (hash.startsWith('/')) {
    return hash.slice(1);
  }
  return hash || 'home';
};

function App() {
  const [route, setRoute] = useState(getRouteFromHash());

  useEffect(() => {
    const handleHashChange = () => setRoute(getRouteFromHash());
    window.addEventListener('hashchange', handleHashChange);
    return () => window.removeEventListener('hashchange', handleHashChange);
  }, []);

  const isVwr = route === 'vwr';
  const isHome = route === 'home';

  return (
    <div className="bg-background min-h-screen flex flex-col">
      <Header />
      {isHome ? <Home /> : isVwr ? <Vwr /> : <CreateEventForm />}
    </div>
  );
}

export default App;
