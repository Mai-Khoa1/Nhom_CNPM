import { configureStore, createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { useDispatch, useSelector, TypedUseSelectorHook } from 'react-redux';
import { horsesAPI, jockeysAPI, schedulesAPI, resultsAPI, Horse, Jockey, Schedule, Result, HorsePayload, JockeyPayload, SchedulePayload, ResultPayload } from '@/lib/api';

// ==================== ASYNC THUNKS ====================

// Horses
export const fetchHorses = createAsyncThunk('horses/fetchAll', async () => {
  const response = await horsesAPI.getAll();
  return response.data;
});

export const createHorse = createAsyncThunk('horses/create', async (horse: HorsePayload) => {
  const response = await horsesAPI.create(horse);
  return response.data;
});

export const updateHorseThunk = createAsyncThunk('horses/update', async ({ id, horse }: { id: number; horse: HorsePayload }) => {
  const response = await horsesAPI.update(id, horse);
  return response.data;
});

export const deleteHorseThunk = createAsyncThunk('horses/delete', async (id: number) => {
  await horsesAPI.delete(id);
  return id;
});

// Jockeys
export const fetchJockeys = createAsyncThunk('jockeys/fetchAll', async () => {
  const response = await jockeysAPI.getAll();
  return response.data;
});

export const createJockey = createAsyncThunk('jockeys/create', async (jockey: JockeyPayload) => {
  const response = await jockeysAPI.create(jockey);
  return response.data;
});

export const updateJockeyThunk = createAsyncThunk('jockeys/update', async ({ id, jockey }: { id: number; jockey: JockeyPayload }) => {
  const response = await jockeysAPI.update(id, jockey);
  return response.data;
});

export const deleteJockeyThunk = createAsyncThunk('jockeys/delete', async (id: number) => {
  await jockeysAPI.delete(id);
  return id;
});

// Schedules
export const fetchSchedules = createAsyncThunk('schedules/fetchAll', async () => {
  const response = await schedulesAPI.getAll();
  return response.data;
});

export const createSchedule = createAsyncThunk('schedules/create', async (schedule: SchedulePayload) => {
  const response = await schedulesAPI.create(schedule);
  return response.data;
});

export const updateScheduleThunk = createAsyncThunk('schedules/update', async ({ id, schedule }: { id: number; schedule: SchedulePayload }) => {
  const response = await schedulesAPI.update(id, schedule);
  return response.data;
});

export const deleteScheduleThunk = createAsyncThunk('schedules/delete', async (id: number) => {
  await schedulesAPI.delete(id);
  return id;
});

// Results
export const fetchResults = createAsyncThunk('results/fetchAll', async () => {
  const response = await resultsAPI.getAll();
  return response.data;
});

export const createResult = createAsyncThunk('results/create', async (result: ResultPayload) => {
  const response = await resultsAPI.create(result);
  return response.data;
});

export const updateResultThunk = createAsyncThunk('results/update', async ({ id, result }: { id: number; result: ResultPayload }) => {
  const response = await resultsAPI.update(id, result);
  return response.data;
});

export const deleteResultThunk = createAsyncThunk('results/delete', async (id: number) => {
  await resultsAPI.delete(id);
  return id;
});

// ==================== SLICES ====================

interface AuthState {
  token: string | null;
  user: { username: string; email: string; role: string } | null;
  isAuthenticated: boolean;
}

const authSlice = createSlice({
  name: 'auth',
  initialState: {
    token: localStorage.getItem('token'),
    user: localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')!) : null,
    isAuthenticated: !!localStorage.getItem('token'),
  } as AuthState,
  reducers: {
    setCredentials: (state, action: PayloadAction<{ token: string; username: string; email: string; role: string }>) => {
      state.token = action.payload.token;
      state.user = { username: action.payload.username, email: action.payload.email, role: action.payload.role };
      state.isAuthenticated = true;
      localStorage.setItem('token', action.payload.token);
      localStorage.setItem('user', JSON.stringify(state.user));
    },
    logout: (state) => {
      state.token = null;
      state.user = null;
      state.isAuthenticated = false;
      localStorage.removeItem('token');
      localStorage.removeItem('user');
    },
  },
});

interface DataState<T> {
  items: T[];
  loading: boolean;
  error: string | null;
}

const horsesSlice = createSlice({
  name: 'horses',
  initialState: { items: [], loading: false, error: null } as DataState<Horse>,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchHorses.pending, (state) => { state.loading = true; state.error = null; })
      .addCase(fetchHorses.fulfilled, (state, action) => { state.loading = false; state.items = action.payload; })
      .addCase(fetchHorses.rejected, (state, action) => { state.loading = false; state.error = action.error.message || 'Failed to fetch horses'; })
      .addCase(createHorse.fulfilled, (state, action) => { state.items.push(action.payload); })
      .addCase(updateHorseThunk.fulfilled, (state, action) => {
        const index = state.items.findIndex((h) => h.id === action.payload.id);
        if (index !== -1) state.items[index] = action.payload;
      })
      .addCase(deleteHorseThunk.fulfilled, (state, action) => {
        state.items = state.items.filter((h) => h.id !== action.payload);
      });
  },
});

const jockeysSlice = createSlice({
  name: 'jockeys',
  initialState: { items: [], loading: false, error: null } as DataState<Jockey>,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchJockeys.pending, (state) => { state.loading = true; state.error = null; })
      .addCase(fetchJockeys.fulfilled, (state, action) => { state.loading = false; state.items = action.payload; })
      .addCase(fetchJockeys.rejected, (state, action) => { state.loading = false; state.error = action.error.message || 'Failed to fetch jockeys'; })
      .addCase(createJockey.fulfilled, (state, action) => { state.items.push(action.payload); })
      .addCase(updateJockeyThunk.fulfilled, (state, action) => {
        const index = state.items.findIndex((j) => j.id === action.payload.id);
        if (index !== -1) state.items[index] = action.payload;
      })
      .addCase(deleteJockeyThunk.fulfilled, (state, action) => {
        state.items = state.items.filter((j) => j.id !== action.payload);
      });
  },
});

const schedulesSlice = createSlice({
  name: 'schedules',
  initialState: { items: [], loading: false, error: null } as DataState<Schedule>,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchSchedules.pending, (state) => { state.loading = true; state.error = null; })
      .addCase(fetchSchedules.fulfilled, (state, action) => { state.loading = false; state.items = action.payload; })
      .addCase(fetchSchedules.rejected, (state, action) => { state.loading = false; state.error = action.error.message || 'Failed to fetch schedules'; })
      .addCase(createSchedule.fulfilled, (state, action) => { state.items.push(action.payload); })
      .addCase(updateScheduleThunk.fulfilled, (state, action) => {
        const index = state.items.findIndex((s) => s.id === action.payload.id);
        if (index !== -1) state.items[index] = action.payload;
      })
      .addCase(deleteScheduleThunk.fulfilled, (state, action) => {
        state.items = state.items.filter((s) => s.id !== action.payload);
      });
  },
});

const resultsSlice = createSlice({
  name: 'results',
  initialState: { items: [], loading: false, error: null } as DataState<Result>,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchResults.pending, (state) => { state.loading = true; state.error = null; })
      .addCase(fetchResults.fulfilled, (state, action) => { state.loading = false; state.items = action.payload; })
      .addCase(fetchResults.rejected, (state, action) => { state.loading = false; state.error = action.error.message || 'Failed to fetch results'; })
      .addCase(createResult.fulfilled, (state, action) => { state.items.push(action.payload); })
      .addCase(updateResultThunk.fulfilled, (state, action) => {
        const index = state.items.findIndex((r) => r.id === action.payload.id);
        if (index !== -1) state.items[index] = action.payload;
      })
      .addCase(deleteResultThunk.fulfilled, (state, action) => {
        state.items = state.items.filter((r) => r.id !== action.payload);
      });
  },
});

// ==================== STORE ====================
export const store = configureStore({
  reducer: {
    auth: authSlice.reducer,
    horses: horsesSlice.reducer,
    jockeys: jockeysSlice.reducer,
    schedules: schedulesSlice.reducer,
    results: resultsSlice.reducer,
  },
});

// ==================== TYPED HOOKS ====================
export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
export const useAppDispatch = () => useDispatch<AppDispatch>();
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector;

// ==================== EXPORTS ====================
export const { setCredentials, logout } = authSlice.actions;