import java.util.Random;
import java.util.Arrays;
import java.math.BigInteger;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.Scanner;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.File;

/*ICS4U1
 * Nicky Lam
 * 3/3/2020
 * This program encrypts and decrypts BigIntegers, encrypts and decrypts files, and generates prime numbers
 */

public class Encryption{
       public static void main(String[] args){
              BigInteger[] primes = generatePrimes();
              BigInteger p = primes[0];
              BigInteger q = primes[1];
              
              BigInteger m = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
              BigInteger n = p.multiply(q);
              
              BigInteger[] keys = generateKeyData(m);
              BigInteger e = keys[0];
              BigInteger d = keys[1];
              
              BigInteger[] publicKey = {n, e};
              BigInteger[] privateKey = {n, d};
              
              Scanner userInput = new Scanner(System.in);              
              Boolean valid = false;
              do{
                     System.out.println("Please enter the pathname of the file you would like to encrypt:");
                     String originalFilePath = userInput.nextLine();
                     originalFilePath = originalFilePath.replace("\\","/");
                     
                     System.out.println("Please enter the pathname of the desired location for the decrypted file:");
                     String decryptedFilePath = userInput.nextLine();
                     decryptedFilePath = decryptedFilePath.replace("\\","/");
                     
                     try{
                            File originalFile = new File (originalFilePath);
                            File encryptedFile = new File ("encryptedFile.txt");
                            File decryptedFile = new File (decryptedFilePath);
                            encryptFile(originalFile, encryptedFile, publicKey);
                            decryptFile(encryptedFile, decryptedFile, privateKey);  
                            valid = true;
                     }catch(Exception error){
                            error.printStackTrace();
                            System.out.println("File not found.");
                     }
              }while (!valid);
       }
       
       /*  This method generates two different prime numbers
        * 
        * @return an array of two different prime numbers
        */
       public static BigInteger[] generatePrimes(){
              Random rnd = new Random();
              
              BigInteger a;
              do{
                     a = new BigInteger(50, rnd); //generate a prime number
              }while(!testPrimality(a, 6));
              
              BigInteger b;
              do{
                     b = new BigInteger(50, rnd);   //generate another prime number that is different from a
              }while ((!testPrimality(b, 6))||(b.compareTo(a) == 0));
              
              BigInteger[] primes = {a, b};
              
              return primes;
       }
       
       /* This method generates the public and private keys
        * 
        * @param m, the value of (p-1)*(q-1)
        * @return an array containing the key pair
        */
       public static BigInteger[] generateKeyData(BigInteger m){
              BigInteger e = new BigInteger("3");
              
              while (m.gcd(e).compareTo(BigInteger.ONE) != 0){         // find an e value that is relatively prime to m
                     e = e.nextProbablePrime();
              }
              
              BigInteger d = e.modInverse(m);       // d is the mod inverse of e
              if (d.compareTo(e) == 0){     // if d is the same as e, make d the next mod inverse of e by adding m
                     d = d.add(m);
              }
              
              BigInteger[] keys = {e, d};
              return keys;
       }
       
       /* This method encrypts a number into a cipher
        * 
        * @param message, the number to be encrypted
        * @param key, an array containing the value of n and the public key
        * @return the encyrpted message
        */
       public static BigInteger encrypt (BigInteger message, BigInteger[] key){
              BigInteger n = key[0];
              BigInteger e = key[1];
              return (message.modPow(e, n));    
       }
       
       /* This method decrypts a cipher into the original message
        * 
        * @param cipher, the number to be decrypted
        * @param key, an array containing the value of n and the private key
        * @return the original message
        */
       public static BigInteger decrypt (BigInteger cipher, BigInteger[] key){
              BigInteger n = key[0];
              BigInteger d = key[1];
              return (cipher.modPow(d, n));
       }
       
       /* This method loads a file and encrypts it into a new file
        * 
        * @param inputFile, the file to be encrypted
        * @param outputFile, the file that the encrypted information will go to
        * @param key, an array containing the value of n and the public key
        */
       public static void encryptFile (File inputFile, File outputFile, BigInteger[] key){
              try{
                     FileInputStream input = new FileInputStream(inputFile);
                     BufferedInputStream bufferedInput = new BufferedInputStream (input);              //read from the original file
                     
                     PrintWriter output = new PrintWriter(outputFile);    //print bytes to the encrypted file
                     
                     int length = (int)inputFile.length();  
                     byte[] bytes = new byte[length];  //create byte array to store read data
                     bufferedInput.read(bytes);  //store data into byte array
                     
                     for (int i = 0; i < length; i++){      
                            BigInteger encryptedByte = encrypt(new BigInteger(Integer.toString((int)bytes[i] & 0xff)), key);   //encrypt each byte, use 0xff to make the byte positive                           
                            output.println(encryptedByte.toString()); //print the byte to encrypted file
                            
                     }
                     
                     bufferedInput.close();
                     output.close();
              }catch(Exception e){
                     e.printStackTrace();
                     System.out.println("File not found.");
              }
       }
       
       /* This method loads an encrypted file and decrypts it back into the original file
        * 
        * @param inputFile, the file to be decrypted
        * @param outputFile, the file that the decrypted information will go to
        * @param key, an array containing the value of n and the private key
        */
       public static void decryptFile (File inputFile, File outputFile, BigInteger[] key){
              try{
                     Scanner countLines = new Scanner(inputFile);  //count lines to determine size of byte array
                     Scanner input = new Scanner(inputFile);  //read the encrypted file
                     
                     FileOutputStream output = new FileOutputStream(outputFile);  // write bytes to decrypted file
                     BufferedOutputStream bufferedOutput = new BufferedOutputStream(output);
                     
                     int length = 0;
                     while (countLines.hasNext()){   //count number of lines in encrypted file
                            length++;
                            countLines.nextLine();
                     }
                     
                     BigInteger[] lines = new BigInteger[length]; 
                     for (int i = 0; i < length; i++){
                            lines[i] = new BigInteger(input.nextLine());
                            lines[i] = decrypt(lines[i], key);  //decrypt each byte
                            bufferedOutput.write((byte)(lines[i].intValue()));  //write decrypted byte to decrypted file
                     }           
                     
                     countLines.close();
                     input.close();
                     bufferedOutput.close();   
              }catch(Exception e){
                     e.printStackTrace();
                     System.out.println("File not found.");
              }
       }
       
       /* This method checks if a small number is prime
        * 
        * @param num, the number to be checked
        * @return if num is prime
        */
       public static boolean isPrime(int num){
              boolean prime = true;
              for(int i = 2; i < num/2; i++){
                     if(num%i == 0){
                            prime = false;
                            break;
                     }
              }
              return prime;
       }
       
       /* This method tests if a number is a prime number
        * 
        * @param n, the number to be checked
        * @param k, the number of times the test is done
        * @return if n is a prime number
        */
       public static boolean testPrimality(BigInteger n, int k){
              if (k == 0){   //when the test has been passed all k times, the number is probably prime
                     return true;
              }
              if (n.mod(new BigInteger("2")).compareTo(BigInteger.ZERO) == 0){  //if number is even, it is not prime
                     return false;
              }else if(n.compareTo(new BigInteger("1000")) == -1){  //if number is less than 1000, can just use integer version
                     return isPrime(n.intValue());
              }
              BigInteger r = BigInteger.ONE;
              BigInteger d = n.subtract(BigInteger.ONE);
              while (n.subtract(BigInteger.ONE).mod(new BigInteger("2").pow(r.intValue())).compareTo(BigInteger.ZERO) != 0){  //increment r until 2^r is a factor of n-1
                     r.add(BigInteger.ONE);
              }
              d = n.subtract(BigInteger.ONE).divide(new BigInteger("2").pow(r.intValue()));  //d is (n-1)/(2^r)
              
              BigInteger a;
              BigInteger max = n.subtract(new BigInteger("2")); //max value of a is n-2
              Random rnd = new Random();
              do{
                     a = new BigInteger(max.bitLength(), rnd);  //generate a random value for a that is between 2 and n-2
              }while(a.compareTo(max) >= 0);
              
              BigInteger x = a.modPow(d, n); //x = a^d mod n
              if ((x.compareTo(BigInteger.ONE) == 0) || (x.compareTo(n.subtract(BigInteger.ONE)) == 0)){  // if x ==1 or x == n-1, the number is probably prime
                     return true;
              }
              boolean checked = false; //sees if primality has been determined
              for (BigInteger i = BigInteger.ZERO; i.compareTo(r.subtract(BigInteger.ONE)) == -1; i = i.add(BigInteger.ONE)){ 
                     x = x.modPow(new BigInteger("2"), n); // x = x^2 mod n
                     if (x.compareTo(BigInteger.ONE) == 0){ // if x == 1 number is not prime
                            checked = true; //primality has been checked
                            return false;
                     }else if(x.compareTo(n.subtract(BigInteger.ONE)) == 0){ // if x == n-1 number is probably prime
                            checked = true; //primality has been checked
                            return true;
                     }
              }
              if (!checked){
                     return false; //if primality has not been determined, the number is probably prime
              }
              
              return testPrimality(n, k-1); //continue to run tests 
       }              
}