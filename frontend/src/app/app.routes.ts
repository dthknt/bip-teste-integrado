import { Routes } from '@angular/router';
import { BeneficioListComponent } from './components/beneficio-list/beneficio-list.component';

/**
 * Define o mapa de rotas da aplicação.
 * A rota raiz (path: '') carrega o BeneficioListComponent.
 */
export const routes: Routes = [
  {
    path: '',
    component: BeneficioListComponent,
  },
  // { path: 'outra-pagina', component: OutroComponent },
  // { path: '**', component: PaginaNaoEncontradaComponent } // Boa prática
];
