import React, { useEffect, useState } from 'react';
import Header from './components/Header';
import CreateEventForm from './pages/CreateEventForm';
import Vwr from './pages/VWR';
import Home from './pages/Home';
import EventDetail from './pages/EventDetail';

const getRouteFromHash = () => {
  const hash = window.location.hash.replace('#', '');
  const normalized = hash.startsWith('/') ? hash.slice(1) : hash;

  if (normalized.startsWith('event/')) {
    const eventId = normalized.split('/')[1];
    return { name: 'event', eventId };
  }

  return { name: normalized || 'home' };
};

function App() {
  const [route, setRoute] = useState(getRouteFromHash());

  useEffect(() => {
    const handleHashChange = () => setRoute(getRouteFromHash());
    window.addEventListener('hashchange', handleHashChange);
    return () => window.removeEventListener('hashchange', handleHashChange);
  }, []);

  const isVwr = route.name === 'vwr';
  const isHome = route.name === 'home';
  const isEventDetail = route.name === 'event';

  return (
    <div className="bg-background min-h-screen flex flex-col">
      <Header />
      {isHome ? (
        <Home />
      ) : isVwr ? (
        <Vwr />
      ) : isEventDetail ? (
        <EventDetail eventId={route.eventId} />
      ) : (
        <CreateEventForm />
      )}
    </div>
  );
}

export default App;
