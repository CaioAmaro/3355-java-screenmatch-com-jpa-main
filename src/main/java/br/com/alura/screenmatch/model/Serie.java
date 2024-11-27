package br.com.alura.screenmatch.model;

import br.com.alura.screenmatch.service.traducao.ConsultaMyMemory;
import jakarta.persistence.*;

import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "series")
public class Serie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String titulo;

    @Enumerated(EnumType.STRING)
    private Categoria genero;

    private String atores;
    private String sinopse;
    private String poster;
    private Integer totalTemporadas;
    private Double avaliacao;

    @Transient
    private List<Episodio> episodios;

    public Serie(){}

   public Serie(DadosSerie dadosSerie){
       this.titulo = dadosSerie.titulo();
       this.genero = Categoria.fromString(dadosSerie.genero().split(",")[0].trim());
       this.atores = dadosSerie.atores();
       this.sinopse = ConsultaMyMemory.obterTraducao(dadosSerie.sinopse());
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
