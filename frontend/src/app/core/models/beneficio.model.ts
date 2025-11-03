export interface Beneficio {
  id: number;
  nome: string;
  descricao: string;
  valor: number;
  ativo: boolean;
}

export type BeneficioCreateDTO = Omit<Beneficio, 'id'>;
