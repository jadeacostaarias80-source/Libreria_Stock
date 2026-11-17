import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Executors;

public class ServidorProyecto {

    static class Libro {
        String titulo;
        String autor;
        int enTienda;
        int online;

        Libro(String titulo, String autor, int enTienda, int online) {
            this.titulo = titulo;
            this.autor = autor;
            this.enTienda = enTienda;
            this.online = online;
        }
    }

    // Código del libro -> datos del libro
    private static final Map<String, Libro> DATOS = Map.of(
            "ORWELL", new Libro("1984", "George Orwell", 25, 5),
            "ANIMALFARM", new Libro("Animal Farm", "George Orwell", 30, 15),
            "CARRIE", new Libro("Carrie", "Stephen King", 25, 16),
            "DORIAN", new Libro("The Picture of Dorian Gray", "Oscar Wilde", 0, 29),
            "GIOVANNI", new Libro("Giovanni's Room", "James Baldwin", 0, 0),
            "SAFO", new Libro("Safo, poemas y fragmentos", "Sappho", 50, 15),
            "CARMILLA", new Libro("Carmilla", "J. Sheridan Le Fanu", 14, 23));

    public static void main(String[] args) throws IOException {
        HttpServer servidor = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 5053), 0);
        servidor.createContext("/consulta", ServidorProyecto::consultar);
        servidor.setExecutor(Executors.newFixedThreadPool(2));
        servidor.start();
        System.out.println("Servidor activo en puerto 5053. Pulsa Stop para detenerlo.");
    }

    private static void consultar(HttpExchange intercambio) throws IOException {
        if (!"GET".equals(intercambio.getRequestMethod())) {
            intercambio.getResponseHeaders().set("Allow", "GET");
            responder(intercambio, 405, "Solo se admite GET");
            return;
        }
        String consulta = intercambio.getRequestURI().getRawQuery();
        if (consulta == null || !consulta.matches("codigo=[A-Z]{1,20}")) {
            responder(intercambio, 400, "Usa ?codigo=CODIGO en mayúsculas");
            return;
        }
        String codigo = consulta.substring("codigo=".length());
        Libro libro = DATOS.get(codigo);
        System.out.println("Código " + codigo + " | PID "
                + ProcessHandle.current().pid() + " | hilo "
                + Thread.currentThread().getName());
        if (libro == null) {
            responder(intercambio, 404, "Código no registrado: " + codigo);
        } else {
            responder(intercambio, 200, libro.titulo + " | " + libro.autor
                    + " | tienda: " + libro.enTienda + " | online: " + libro.online);
        }
    }

    private static void responder(HttpExchange intercambio, int estado,
                                  String mensaje) throws IOException {
        byte[] datos = mensaje.getBytes(StandardCharsets.UTF_8);
        intercambio.getResponseHeaders().set(
                "Content-Type", "text/plain; charset=UTF-8");
        intercambio.sendResponseHeaders(estado, datos.length);
        try (var salida = intercambio.getResponseBody()) {
            salida.write(datos);
        }
    }
}
