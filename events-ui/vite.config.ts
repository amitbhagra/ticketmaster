import { defineConfig } from 'vite';
import angular from '@analogjs/vite-plugin-angular';

export default defineConfig({
  root: './src',
  plugins: [angular()],
  server: {
    port: 4200,
    open: true,
    hmr: true,
    host: true // <-- This exposes the dev server to your local network
  }
});