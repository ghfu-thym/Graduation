import React, { useEffect, useState } from 'react';
import Header from './components/Header';
import CreateEventForm from './pages/CreateEventForm';
import Vwr from './pages/VWR';
import Home from './pages/Home';
import EventDetail from './pages/EventDetail';
import AuthPage from './pages/Auth';
import ChooseTicket from './pages/ChooseTicket';

const LAST_ROUTE_KEY = 'lastNonAuthRoute';
const REDIRECT_KEY = 'postLoginRedirect';

const getRouteFromHash = () => {
  const hash = window.location.hash.replace('#', '');
  const normalized = hash.startsWith('/') ? hash.slice(1) : hash;

  if (normalized.startsWith('event/')) {
    const eventId = normalized.split('/')[1];
    return { name: 'event', eventId };
  }

  if (normalized.startsWith('vwr/')) {
    const eventId = normalized.split('/')[1];
    return { name: 'vwr', eventId };
  }

  if (normalized.startsWith('choose-ticket')) {
    const parts = normalized.split('/');
    const eventId = parts.length > 1 ? parts[1] : undefined;
    return { name: 'choose-ticket', eventId };
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
  const isAuth = route.name === 'login' || route.name === 'register';
  const isCreateEvent = route.name === 'create-event';
  const isChooseTicket = route.name === 'choose-ticket';
  const authToken = localStorage.getItem('authToken');

  useEffect(() => {
    const currentHash = window.location.hash || '#/home';

    if (!isAuth) {
      localStorage.setItem(LAST_ROUTE_KEY, currentHash);
      return;
    }

    if (!localStorage.getItem(REDIRECT_KEY)) {
      const lastRoute = localStorage.getItem(LAST_ROUTE_KEY) || '#/home';
      localStorage.setItem(REDIRECT_KEY, lastRoute);
    }
  }, [isAuth, route.name, route.eventId]);

  useEffect(() => {
    if (isCreateEvent && !authToken) {
      localStorage.setItem(REDIRECT_KEY, '#/create-event');
      window.location.hash = '#/login';
    }
  }, [isCreateEvent, authToken]);

  return (
    <div className="bg-background min-h-screen flex flex-col">
      <Header />
      {isHome ? (
        <Home />
      ) : isVwr ? (
        <Vwr eventId={route.eventId} />
      ) : isChooseTicket ? (
        <ChooseTicket eventId={route.eventId} />
      ) : isEventDetail ? (
        <EventDetail eventId={route.eventId} />
      ) : isAuth ? (
        <AuthPage initialMode={route.name} />
      ) : (
        <CreateEventForm />
      )}
    </div>
  );
}

export default App;
