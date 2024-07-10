package challenge.one;

import com.google.gson.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class Main {
    private static final int REAL_BRASILEIRO = 1;
    private static final int DOLAR_AMERICANO = 2;
    private static final int PESO_ARGENTINO = 3;
    
    private static final int PESO_COLOMBIANO = 4;
    private static final int SAIR = 5;

    public static void main(String[] args) {
        boolean sair = false;
        Scanner scanner = new Scanner(System.in);

        while (!sair) {
            int opcao1 = mostrarMenu(scanner, "Escolha a moeda de origem:");
            if (opcao1 == SAIR) {
                sair = true;
                break;
            }

            int opcao2;
            do {
                opcao2 = mostrarMenu(scanner, "Escolha a moeda de destino:");
                if (opcao2 == SAIR) {
                    sair = true;
                    break;
                }

                if (opcao1 == opcao2) {
                    System.out.println("Não é possível converter uma moeda para a mesma moeda. Tente novamente.");
                }
            } while (opcao1 == opcao2);

            if (sair) {
                break;
            }

            System.out.println("De: " + opcaoToString(opcao1));

            // Verifica se a opção escolhida para destino é válida
            String moedaDestino = opcaoToString(opcao2);
            if (moedaDestino.equals("Opção inválida")) {
                System.out.println("Opção de moeda de destino inválida. Tente novamente.");
                continue;
            }
            System.out.println("Para: " + moedaDestino);

            System.out.println("Qual o valor que deseja converter? (Ex: 75.50)");
            double valor = scanner.nextDouble();

            ResultadoConversao resultado = converterMoeda(opcao1, opcao2, valor);
            if (resultado != null) {
                System.out.printf("Valor convertido de %s para %s: %.2f %s%n",
                        opcaoToString(opcao1), moedaDestino,
                        resultado.getValorConvertido(), resultado.getCodigoMoedaPara());
            } else {
                System.out.println("Erro ao converter moeda. Verifique sua conexão ou tente novamente mais tarde.");
            }
        }
        scanner.close();
    }

    private static int mostrarMenu(Scanner scanner, String mensagem) {
        System.out.println(mensagem);
        System.out.println("1. Real brasileiro");
        System.out.println("2. Dólar americano");
        System.out.println("3. Peso argentino");
        System.out.println("4. Peso colombiano");
        System.out.println("5. Sair | Encerrar programa");
        return scanner.nextInt();
    }

    private static String opcaoToString(int opcao) {
        switch (opcao) {
            case REAL_BRASILEIRO:
                return "Real brasileiro";
            case DOLAR_AMERICANO:
                return "Dólar americano";
            case PESO_ARGENTINO:
                return "Peso argentino";
            case PESO_COLOMBIANO:
                return "Peso colombiano";
            default:
                return "Opção inválida";
        }
    }

    @SuppressWarnings("deprecation")
    private static ResultadoConversao converterMoeda(int de, int para, double valor) {
        try {
            String codigoMoedaDe = pegarSigla(de);
            String codigoMoedaPara = pegarSigla(para);

            if (codigoMoedaDe.isEmpty() || codigoMoedaPara.isEmpty()) {
                return null; // Retorna null se não encontrar a sigla da moeda
            }

            String apiKey = "a4f264f5505faabe9c4219e2";
            String url_str = "https://v6.exchangerate-api.com/v6/" + apiKey + "/latest/" + codigoMoedaDe;

            URL url = new URL(url_str);
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.connect();

            JsonParser jp = new JsonParser();
            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
            JsonObject jsonobj = root.getAsJsonObject();

            String req_result = jsonobj.get("result").getAsString();
            if (!"success".equals(req_result)) {
                System.out.println("Erro ao obter a taxa de conversão.");
                return null;
            }

            JsonObject conversionRates = jsonobj.getAsJsonObject("conversion_rates");
            double taxaDeConversao = conversionRates.get(codigoMoedaPara).getAsDouble();

            return new ResultadoConversao(valor * taxaDeConversao, codigoMoedaPara);
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
            return null;
        }
    }

    private static String pegarSigla(int opcao) {
        switch (opcao) {
            case REAL_BRASILEIRO:
                return "BRL";
            case DOLAR_AMERICANO:
                return "USD";
            case PESO_ARGENTINO:
                return "ARS";
            case PESO_COLOMBIANO:
                return "COP";
            default:
                return "";
        }
    }
}

class ResultadoConversao {
    private double valorConvertido;
    private String codigoMoedaPara;

    public ResultadoConversao(double valorConvertido, String codigoMoedaPara) {
        this.valorConvertido = valorConvertido;
        this.codigoMoedaPara = codigoMoedaPara;
    }

    public double getValorConvertido() {
        return valorConvertido;
    }

    public String getCodigoMoedaPara() {
        return codigoMoedaPara;
    }
}
