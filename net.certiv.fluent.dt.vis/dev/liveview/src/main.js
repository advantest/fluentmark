import {createApp} from 'vue';

import App from './App.vue';
const app = createApp(App);

import {store} from './store';
app.use(store);

import hljs from 'highlight.js';
app.use(hljs.vuePlugin);

import PrimeVue from 'primevue/config';
import Button from 'primevue/button';
import Card from 'primevue/card';
import Toolbar from 'primevue/toolbar';
import 'primevue/resources/themes/saga-blue/theme.css';
import 'primevue/resources/primevue.min.css';
import 'primeicons/primeicons.css';
import 'primeflex/primeflex.css';
import './assets/layout/layout.scss';

import './assets/markdown-body/index.scss';

app.use(PrimeVue);
app.component('Button', Button);
app.component('Card', Card);
app.component('Toolbar', Toolbar);

import VueNativeSock from 'vue-native-websocket-vue3';
app.use(VueNativeSock, 'ws://localhost:9025/liveview', {
  store: store,
  format: 'json',
  reconnection: true,
  reconnectionAttempts: 5,
  reconnectionDelay: 3000,
});

app.mount('#app');
