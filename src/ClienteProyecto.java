import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class ClienteProyecto {
    public static void main(String[] args) throws InterruptedException {
        // Libros con stock, uno con 0 y uno que no existe (404)
        String[] codigos = {"SAFO", "DORIAN", "GIOVANNI", "ORWELL",
                "CARRIE", "CARMILLA", "NOEXISTE"};

        List<Thread> hilos = new ArrayList<>();
        // Primero se crean e inician todos los hilos...
        for (String codigo : codigos) {
            Thread hilo = new Thread(() -> consultar(codigo), "Cliente-" + codigo);
            hilos.add(hilo);
            hilo.start();
        }
        // ...y después se espera a que terminen todos
        for (Thread hilo : hilos) {
            hilo.join();
        }
        System.out.println("Consultas terminadas.");
    }

    private static void consultar(String codigo) {
        String direccion = "http://127.0.0.1:5053/consulta?codigo=" + codigo;
        HttpRequest solicitud = HttpRequest.newBuilder(
                URI.create(direccion)).GET().build();
        try {
            HttpResponse<String> respuesta = HttpClient.newHttpClient().send(
                    solicitud, HttpResponse.BodyHandlers.ofString());
            System.out.println(codigo + " | HTTP " + respuesta.statusCode()
                    + " | " + respuesta.body());
        } catch (IOException e) {
            System.out.println(codigo + " | Servidor no disponible: "
                    + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println(codigo + " | Consulta interrumpida");
        }
    }
}