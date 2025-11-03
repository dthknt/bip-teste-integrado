import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';

/**
 * Arquivo de entrada (bootstrap) da aplicação.
 * Carrega o AppComponent e as configurações (appConfig).
 */
bootstrapApplication(AppComponent, appConfig).catch((err) =>
  console.error(err)
);
