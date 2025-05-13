import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Exchanger;
import java.util.concurrent.Semaphore;

public class Example {
    public static void CurrentThreadExample(){
        var currentThread = Thread.currentThread();
        System.out.println(currentThread.getName());
        currentThread.setPriority(9);
        System.out.println(currentThread.getPriority());
    }

    public static void CreateDisableThreadExample() throws InterruptedException {
        //        Thread.currentThread().setName("Main");

        MyThread myThread = new MyThread("My Thread");
        myThread.start();

//        Thread runThread = new Thread(new RunnableThread(), "Run thread");
//        runThread.start();

        for(int i = 0; i < 20; i++){
            System.out.println(Thread.currentThread().getName() + ": " + (i + 1));
            try {
                Thread.sleep(500);
                if(i == 8)
                    //myThread.disable();
                    myThread.interrupt();
            }
            catch(Exception ex){
                System.out.println(ex.getMessage());
            }
        }

        myThread.join();
        //runThread.join();
    }

    public static void SynchronizedThreadsExample() throws InterruptedException {
        Resource shareResource = new Resource();
        ArrayList<Thread> threads = new ArrayList<>();

        for(int i = 0; i < 5; i++){
            Thread thread = new Thread(new CounterThread(shareResource), "Thread #" + (i + 1));
            threads.add(thread);
            thread.start();
        }

        for(var t : threads){
            t.join();
        }

        System.out.println(shareResource.Count);
    }

    public static void WaitNotifySemaphoreExample(){
        // Parking parking = new Parking();
        Semaphore semaphore = new Semaphore(Parking.MAX_CARS, false);

        for(int i = 0; i < 5; i++){
            Car car = new Car(semaphore);
            new Thread(car, "Car #" + (i + 1)).start();
        }
    }

    public static void ExchangerExample(){
        Exchanger<String> exMessage = new Exchanger<>();
        new Thread(new Client(exMessage)).start();
        new Thread(new Server(exMessage)).start();
    }
}

class MyThread extends Thread{
    boolean isActive;

    public MyThread(String name){
        super(name);
        isActive = true;
    }

    public void disable(){
        isActive = false;
    }

    public void run(){
        //for(int i = 0; i < 10; i++)
        int i = 0;
        //while(isActive){
//        while(!isInterrupted()){
//            System.out.println(Thread.currentThread().getName() + ": " + (i + 1));
//            try {
//                Thread.sleep(500);
//            }
//            catch(Exception ex){
//                System.out.println(ex.getMessage());
//                //interrupt();
//                break;
//            }
//            i++;
//        }

        try{
            while(!isInterrupted()){
                System.out.println(Thread.currentThread().getName() + ": " + (i + 1));
                Thread.sleep(500);
                i++;
            }
        }
        catch(Exception ex){
            System.out.println(ex.getMessage());
        }
    }
}

class RunnableThread implements Runnable{

    @Override
    public void run() {
        for(int i = 0; i < 10; i++){
            System.out.println(Thread.currentThread().getName() + ": " + (i + 1));
            try {
                Thread.sleep(500);
            }
            catch(Exception ex){
                System.out.println(ex.getMessage());
            }
        }
    }
}

class Resource{
    public int Count = 0;

    public void Increment(){
        Count++;
    }
}

class CounterThread implements Runnable{
    Resource resource;

    public CounterThread(Resource resource){
        this.resource = resource;
    }
    @Override
    public void run() {
        for(int i = 0; i < 10000; i++){
            synchronized (resource){
                resource.Increment();
                //System.out.println(Thread.currentThread().getName() + ": " + resource.Count);
            }
        }
    }
}

class Parking{
    public static final int MAX_CARS = 3;

    ArrayList<Car> cars = new ArrayList<>();

    //int cars = 0;

    public synchronized void entry(Car car){

        while(cars.size() >= MAX_CARS){
            try{
                wait();
            }
            catch (Exception ex){
                System.out.println(ex.getMessage());
            }
        }
        //cars++;
        cars.add(car);
        System.out.println(car.getName() + " entry to parking");
        System.out.println("Cars on parking: " + cars.size());
        notify();
    }

    public synchronized void leave(Car car){
        while(cars.size() < 1){
            try{
                wait();
            }
            catch (Exception ex){
                System.out.println(ex.getMessage());
            }
        }
        //cars--;
        cars.remove(car);
        System.out.println(car.getName() + " leave from parking");
        System.out.println("Cars on parking: " + cars.size());
        notify();
    }
}

class Car implements Runnable{
    Parking parking;
    boolean isEntry;
    Semaphore semaphore;

    //    public Car(Parking parking, Semaphore semaphore){
//        this.parking = parking;
//        this.semaphore = semaphore;
//        isEntry = false;
//    }
    public Car(Semaphore semaphore){
        this.semaphore = semaphore;
        isEntry = false;
    }

    public String getName(){
        return Thread.currentThread().getName();
    }

    @Override
    public void run() {
        Random random = new Random();

        for(int i = 0; i < 6; i++){

//            try{
//
//            }
//            catch (Exception ex){
//                System.out.println(ex.getMessage());
//            }

            try{
                //if(isEntry){
                //parking.leave(this);
                Thread.sleep(random.nextInt(1000, 2000));
                System.out.println(this.getName() + " wait parking");

                semaphore.acquire();
                isEntry = false;
                System.out.println(this.getName() + " entry to parking");
                //System.out.println("Cars on parking: " + semaphore.toString());

                Thread.sleep(random.nextInt(1000, 2000));
//                }
//                else{
                //parking.entry(this);
                semaphore.release();
                isEntry = true;
                System.out.println(this.getName() + " leave from parking");
                //System.out.println("Cars on parking: " + semaphore.toString());
                //}
            }
            catch (Exception ex){
                System.out.println(ex.getMessage());
            }


        }
    }
}

class Server implements Runnable{
    Exchanger<String> exchanger;
    String message;

    public Server(Exchanger<String> exchanger){
        this.exchanger = exchanger;
    }
    @Override
    public void run() {
        try{
            message = exchanger.exchange(message);
            System.out.println("Server receive message: " + message);
        }
        catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }
}

class Client implements Runnable{
    Exchanger<String> exchanger;
    String message;

    public Client(Exchanger<String> exchanger){
        this.exchanger = exchanger;
    }
    @Override
    public void run() {
        try{
            Scanner scanner = new Scanner(System.in);
            message = scanner.nextLine();

            exchanger.exchange(message);
            System.out.println("Client send message: " + message);
        }
        catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }
}