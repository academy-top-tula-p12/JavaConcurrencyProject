import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Exchanger;
import java.util.concurrent.Phaser;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Storehouse storehouse = new Storehouse();

        for(int i = 0; i < 3; i++){
            Supplier supplier = new Supplier(storehouse, "Supplier #" + (i + 1));
            Consumer consumer = new Consumer(storehouse, "Consumer #" + (i + 1));
            new Thread(supplier).start();
            new Thread(consumer).start();
        }
    }
}

class Storehouse{
    int products;
    ReentrantLock locker;
    Condition condition;
    public final int MAX_PRODUCTS = 6;

    Storehouse(){
        locker = new ReentrantLock();
        condition = locker.newCondition();
    }

    public void receiving(){
        locker.lock();
        try{
            while(products >= MAX_PRODUCTS){
                condition.await();
            }
            products++;
            System.out.println("Add ONE product. Total: " + products);
            condition.signalAll();
        }
        catch(Exception ex){
            System.out.println(ex.getMessage());
        }
        finally {
            locker.unlock();
        }
    }

    public void shipment(){
        locker.lock();
        try{
            while(products < 1){
                condition.await();
            }

            products--;
            System.out.println("Remove ONE product. Total: " + products);

            condition.signalAll();
        }
        catch(Exception ex){
            System.out.println(ex.getMessage());
        }
        finally {
            locker.unlock();
        }
    }
}

class Supplier implements Runnable{
    Storehouse storehouse;
    String name;

    public Supplier(Storehouse storehouse, String name){
        this.storehouse = storehouse;
        this.name = name;
    }

    @Override
    public void run() {
        Random random = new Random();
        final int MAX_PRODUCTS = 10;

        for(int i = 0; i < MAX_PRODUCTS; i++){
            try {
                Thread.sleep(random.nextInt(500, 1000));
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            storehouse.receiving();
            System.out.println("Supplier: " + name + " add ONE product");
        }
    }
}

class Consumer implements Runnable{
    Storehouse storehouse;
    String name;

    public Consumer(Storehouse storehouse, String name){
        this.storehouse = storehouse;
        this.name = name;
    }

    @Override
    public void run() {
        Random random = new Random();
        final int MAX_PRODUCTS = 10;

        for(int i = 0; i < MAX_PRODUCTS; i++){
            try {
                Thread.sleep(random.nextInt(500, 2000));
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            storehouse.shipment();
            System.out.println("Consumer: " + name + " remove ONE product");
        }
    }
}



