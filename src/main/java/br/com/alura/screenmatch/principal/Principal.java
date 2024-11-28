package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";

    private List<Serie> series;
    private List<DadosSerie> dadosSeries = new ArrayList<>();

    private SerieRepository repositorio;

    private Optional<Serie> serieBuscada;

    public Principal(SerieRepository repositorio){
        this.repositorio = repositorio;
    }

    public void exibeMenu() {
        var opcao = -1;
        while(opcao != 0) {
            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar Séries Buscadas
                    4 - Buscar Série por Titulo
                    5 - Buscar Série por Ator/Atriz
                    6 - Listar Top 5 Séries
                    7 - Buscar Série por gênero
                    8 - Buscar Séries por Temporadas e Avaliação (Desafio)
                    9 - Buscar Séries por Trecho do Episodio
                    10 - Top Episodios Por Séries
                    11 - Buscar Episodios apartir de uma data.
                    
                    0 - Sair
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriePorAtores();
                    break;
                case 6:
                    listarTop5Series();
                    break;
                case 7:
                    buscarSeriePorGenero();
                    break;
                case 8:
                    buscarSeriesPorTemporadas();
                    break;
                case 9:
                    buscarSeriePorTrecho();
                    break;
                case 10:
                    listarTopEpisodiosPorSerie();
                    break;
                case 11:
                    buscarSeriePorApartirData();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);
        repositorio.save(serie);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie(){

        System.out.println("Digite o nome da Série: ");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> serie = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if(serie.isPresent()){
            var serieEncontrada = serie.get();

            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(dt -> dt.episodios().stream()
                            .map(e -> new Episodio(dt.numero(), e)))
                    .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);

            temporadas.forEach(System.out::println);

        }else{
            System.out.println("Serie não encontrada.");
        }


    }

    private  void listarSeriesBuscadas(){
        series = repositorio.findAll();

        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    public void buscarSeriePorTitulo(){

        System.out.println("Digite o nome da Série para pesquisar: ");
        var nomeSerie = leitura.nextLine();

        serieBuscada = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if(serieBuscada.isPresent()){
            System.out.println("A Série Buscada é: " + serieBuscada.get());
        }else{
            System.out.println("Série não encontrada");
        }

    }

    public void buscarSeriePorAtores(){
        System.out.println("Digite o nome do Ator/Atriz: ");
        var nomeAtor = leitura.nextLine();
        System.out.println("Digite a avaliação desejada: ");
        var avaliacao = leitura.nextDouble();

        List<Serie> series = repositorio.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, avaliacao);

        System.out.println("Séries em que "+ nomeAtor + " Trabalhou e que tiveram avaliacao igual ou melhor que ["+avaliacao+"]");
        series.forEach(s ->
                System.out.println("Série: "+ s.getTitulo()+", Avaliação: "+ s.getAvaliacao()));

    }

    public void listarTop5Series(){
        List<Serie> seriesTop = repositorio.findTop5ByOrderByAvaliacaoDesc();

        seriesTop.forEach(s -> System.out.println("Série: "+ s.getTitulo()+", Avaliação: "+ s.getAvaliacao()));
    }

    public void buscarSeriePorGenero(){
        System.out.println("Digite o gênero: ");
        var generoEscolhido = leitura.nextLine();

        var categoria = Categoria.fromPortugues(generoEscolhido);

        List<Serie> series = repositorio.findByGenero(categoria);

        System.out.println("Lista de Séries Filtradas por "+ categoria);
        series.forEach(System.out::println);
    }

    public void buscarSeriesPorTemporadas(){

        System.out.println("Digite quantas temporadas máximo você quer: ");
        var temporadasMax = leitura.nextInt();
        leitura.nextLine();

        System.out.println("Digite qual apartir de qual avaliação você deseja filtrar: ");
        var avaliacao = leitura.nextDouble();

        List<Serie> seriesfiltradas = repositorio.seriesPorTemporadaEAvaliacao(temporadasMax,avaliacao);

        seriesfiltradas.forEach(System.out::println);

    }

    public void buscarSeriePorTrecho(){

        System.out.println("Digite o trecho do episodio procurado: ");
        var trechoEpisodio = leitura.nextLine();

        List<Episodio> episodios = repositorio.episodiosPorTrecho(trechoEpisodio);

        episodios.forEach( e ->
                System.out.printf("Série: %s , Temporada: %s , Episódio %s - %s\n" ,
                        e.getSerie().getTitulo(), e.getTemporada(), e.getNumeroEpisodio(), e.getTitulo()) );

    }

    public void listarTopEpisodiosPorSerie(){

        buscarSeriePorTitulo();

        Serie serie = serieBuscada.get();

        if(serieBuscada.isPresent()){
            List<Episodio> episodios = repositorio.ListarTopEpisodios(serie);
            episodios.forEach(e ->
                    System.out.printf("Série: %s , Temporada: %s , Episódio %s - %s , Avaliação: %s\n" ,
                            e.getSerie().getTitulo(), e.getTemporada(), e.getNumeroEpisodio(), e.getTitulo(), e.getAvaliacao() ) );
        }
    }

    public void buscarSeriePorApartirData(){
        buscarSeriePorTitulo();

        if (serieBuscada.isPresent()){
            System.out.println("Apartir de Qual ano você quer realizar essa busca? ");
            var ano = leitura.nextInt();
            leitura.nextLine();
            Serie serie = serieBuscada.get();
            List<Episodio> listaEpisodios = repositorio.ListarEpisodioApartirDeUmaData(serie, ano);
            listaEpisodios.forEach(System.out::println);
        }


    }

}