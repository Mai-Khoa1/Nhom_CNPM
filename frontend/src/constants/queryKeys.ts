export const queryKeys = {
  auth: { me: ['auth', 'me'] as const },

  horses: {
    all: ['horses'] as const,
    list: (params: object) => ['horses', 'list', params] as const,
    detail: (id: string) => ['horses', id] as const,
    raceHistory: (id: string) => ['horses', id, 'race-history'] as const,
    health: (id: string) => ['horses', id, 'health'] as const,
    doping: (id: string) => ['horses', id, 'doping'] as const,
  },

  jockeys: {
    all: ['jockeys'] as const,
    list: (params: object) => ['jockeys', 'list', params] as const,
    detail: (id: string) => ['jockeys', id] as const,
  },

  races: {
    all: ['races'] as const,
    list: (params: object) => ['races', 'list', params] as const,
    detail: (id: string) => ['races', id] as const,
    registrations: (id: string) => ['races', id, 'registrations'] as const,
    lanes: (id: string) => ['races', id, 'lanes'] as const,
  },

  seasons: {
    all: ['seasons'] as const,
    list: (params: object) => ['seasons', 'list', params] as const,
    detail: (id: string) => ['seasons', id] as const,
    pointRules: (id: string) => ['seasons', id, 'point-rules'] as const,
  },

  registrations: {
    all: ['registrations'] as const,
    list: (params: object) => ['registrations', 'list', params] as const,
    my: (params: object) => ['registrations', 'my', params] as const,
    detail: (id: string) => ['registrations', id] as const,
  },

  results: {
    byRace: (raceId: string) => ['results', 'race', raceId] as const,
  },

  rankings: {
    horses: (params: object) => ['rankings', 'horses', params] as const,
    jockeys: (params: object) => ['rankings', 'jockeys', params] as const,
  },

  notifications: {
    list: (params: object) => ['notifications', 'list', params] as const,
    unreadCount: ['notifications', 'unread-count'] as const,
  },

  users: {
    list: (params: object) => ['users', 'list', params] as const,
    detail: (id: string) => ['users', id] as const,
  },

  auditLogs: {
    list: (params: object) => ['audit-logs', 'list', params] as const,
  },

  dashboard: {
    stats: ['dashboard', 'stats'] as const,
    upcoming: ['dashboard', 'upcoming'] as const,
    recent: ['dashboard', 'recent'] as const,
  },
};