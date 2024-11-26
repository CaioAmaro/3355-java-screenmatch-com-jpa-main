package br.com.alura.screenmatch.model;

import java.util.Optional;

public class Serie {
    private String titulo;
    private Categoria genero;
    private String atores;
    private String sinopse;
    private String poster;
    private Integer totalTemporadas;
    private Double avaliacao;

   public Serie(DadosSerie dadosSerie){
       this.titulo = dadosSerie.titulo();
       this.genero = Categoria.fromString(dadosSerie.genero().split(",")[0].trim());
       this.atores = dadosSerie.atores();
       this.sinopse = dadosSerie.sinopse();
       this.poster = dadosSerie.poster();
       this.totalTemporadas = dadosSerie.totalTemporadas();
       this.avaliacao = Optional.of(Double.valueOf(dadosSerie.avaliacao())).orElse(0.0);
   }

    public Categoria getGenero() {
        return genero;
    }

    @Override
    public String toString() {
        return  "titulo='" + titulo + '\'' +
                ", genero=" + genero +
                ", atores='" + atores + '\'' +
                ", sinopse='" + sinopse + '\'' +
                ", poster='" + poster + '\'' +
                ", totalTemporadas=" + totalTemporadas +
                ", avaliacao=" + avaliacao;
    }
}
