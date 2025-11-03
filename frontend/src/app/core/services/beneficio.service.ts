import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Beneficio, BeneficioCreateDTO } from '../models/beneficio.model';

export interface BeneficioTransferDTO {
  idBeneficioOrigem: number;
  idBeneficioDestino: number;
  valor: number;
}

/**
 * Serviço responsável por toda a comunicação HTTP
 * com a API de Benefícios
 */
@Injectable({
  providedIn: 'root',
})
export class BeneficioService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1/beneficios';

  getBeneficios(): Observable<Beneficio[]> {
    return this.http
      .get<Beneficio[]>(this.apiUrl)
      .pipe(catchError(this.handleError));
  }

  createBeneficio(beneficio: BeneficioCreateDTO): Observable<Beneficio> {
    return this.http
      .post<Beneficio>(this.apiUrl, beneficio)
      .pipe(catchError(this.handleError));
  }

  updateBeneficio(
    id: number,
    beneficio: BeneficioCreateDTO
  ): Observable<Beneficio> {
    return this.http
      .put<Beneficio>(`${this.apiUrl}/${id}`, beneficio)
      .pipe(catchError(this.handleError));
  }

  deleteBeneficio(id: number): Observable<void> {
    return this.http
      .delete<void>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  transferir(payload: BeneficioTransferDTO): Observable<void> {
    return this.http
      .post<void>(`${this.apiUrl}/transferir`, payload)
      .pipe(catchError(this.handleError));
  }

  private handleError(error: HttpErrorResponse) {
    console.error('Erro na API:', error);
    let userMessage = 'Erro ao processar a solicitação. Tente novamente.';
    return throwError(() => new Error(userMessage));
  }
}
