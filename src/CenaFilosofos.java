import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

class Tenedor {
    private final Semaphore semaphore;
    private final int id;

    public Tenedor(int id) {
        this.id = id;
        this.semaphore = new Semaphore(1);
    }

    public boolean tomar(int filosofoId) throws InterruptedException {
        if (semaphore.tryAcquire(500, TimeUnit.MILLISECONDS)) {
            System.out.println("Filósofo " + filosofoId + " toma tenedor " + id);
            return true;
        }
        return false;
    }

    public void soltar(int filosofoId) {
        semaphore.release();
        System.out.println("Filósofo " + filosofoId + " suelta tenedor " + id);
    }
}

class Filosofo extends Thread {
    private final int id;
    private final Tenedor tenedorIzquierdo;
    private final Tenedor tenedorDerecho;
    private int comidas = 0;
    private enum Estado { PENSANDO, HAMBRIENTO, COMIENDO }
    private Estado estado = Estado.PENSANDO;

    public Filosofo(int id, Tenedor izquierdo, Tenedor derecho) {
        this.id = id;
        this.tenedorIzquierdo = izquierdo;
        this.tenedorDerecho = derecho;
    }

    private void pensar() throws InterruptedException {
        estado = Estado.PENSANDO;
        System.out.println("Filósofo " + id + " está pensando");
        Thread.sleep((long) (Math.random() * 1000));
    }

    private void comer() throws InterruptedException {
        estado = Estado.HAMBRIENTO;
        System.out.println("Filósofo " + id + " tiene hambre");

        while (true) {
            if (tenedorIzquierdo.tomar(id)) {
                if (tenedorDerecho.tomar(id)) {
                    estado = Estado.COMIENDO;
                    System.out.println("Filósofo " + id + " está comiendo");
                    Thread.sleep((long) (Math.random() * 1000));
                    comidas++;
                    tenedorDerecho.soltar(id);
                    tenedorIzquierdo.soltar(id);
                    break;
                } else {
                    tenedorIzquierdo.soltar(id);
                }
            }
            Thread.sleep(100);
        }
    }

    @Override
    public void run() {
        try {
            while (comidas < 3) {  // Cada filósofo comerá 3 veces
                pensar();
                comer();
            }
            System.out.println("Filósofo " + id + " ha terminado de comer");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class CenaFilosofos {
    private static final int NUM_FILOSOFOS = 5;

    public static void main(String[] args) {
        Tenedor[] tenedores = new Tenedor[NUM_FILOSOFOS];
        Filosofo[] filosofos = new Filosofo[NUM_FILOSOFOS];

        // Inicializar tenedores
        for (int i = 0; i < NUM_FILOSOFOS; i++) {
            tenedores[i] = new Tenedor(i);
        }

        // Inicializar filósofos
        for (int i = 0; i < NUM_FILOSOFOS; i++) {
            filosofos[i] = new Filosofo(i, tenedores[i], tenedores[(i + 1) % NUM_FILOSOFOS]);
        }

        // Iniciar simulación
        for (Filosofo filosofo : filosofos) {
            filosofo.start();
        }

        // Esperar a que todos terminen
        for (Filosofo filosofo : filosofos) {
            try {
                filosofo.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("Simulación completada");
    }
}
