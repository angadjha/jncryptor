/*    Copyright 2013 Duncan Jones
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cryptonode.jncryptor;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 * Tests for the {@link AES256v2CipherInputStream} class.
 */
public class AES256v2CipherInputStreamTest {

  private static final Random RANDOM = new Random();

  /**
   * Test reading using read() method.
   * 
   * @throws Exception
   */
  @Test
  public void testUsingRead() throws Exception {
    byte[] plaintext = getRandomBytes(256);

    final String password = "Testing1234";

    JNCryptor cryptor = new AES256v2Cryptor();
    byte[] data = cryptor.encryptData(plaintext, password.toCharArray());

    InputStream in = new AES256v2CipherInputStream(new ByteArrayInputStream(
        data), password.toCharArray());

    try {

      byte[] result = new byte[256];
      int offset = 0;
      int b;
      while ((b = in.read()) != -1) {
        result[offset++] = (byte) b;
      }

      assertTrue(offset == plaintext.length);
      assertArrayEquals(plaintext, result);

    } finally {
      in.close();
    }
  }

  /**
   * Test reading using read(byte[]) method.
   * 
   * @throws Exception
   */
  @Test
  public void testUsingReadByteArray() throws Exception {
    byte[] plaintext = getRandomBytes(256);

    final String password = "Testing1234";

    JNCryptor cryptor = new AES256v2Cryptor();
    byte[] data = cryptor.encryptData(plaintext, password.toCharArray());

    InputStream in = new AES256v2CipherInputStream(new ByteArrayInputStream(
        data), password.toCharArray());

    try {

      byte[] result = new byte[256];
      IOUtils.readFully(in, result);

      assertArrayEquals(plaintext, result);
    } finally {
      in.close();
    }
  }

  /**
   * Test reading of mismatched data (e.g. encrypte using keys, decrypted with
   * password).
   * 
   * @throws Exception
   */
  @Test(expected = IOException.class) // TODO check for specific message
  public void testUsingMismatchedKeys() throws Exception {

    byte[] plaintext = getRandomBytes(256);
    byte[] encryptionSalt = getRandomBytes(8);
    byte[] hmacSalt = getRandomBytes(8);

    final String password = "Testing1234";

    JNCryptor cryptor = new AES256v2Cryptor();

    SecretKey hmacKey = cryptor
        .keyForPassword(password.toCharArray(), hmacSalt);
    SecretKey encryptionKey = cryptor.keyForPassword(password.toCharArray(),
        encryptionSalt);

    byte[] data = cryptor.encryptData(plaintext, encryptionKey, hmacKey);

    InputStream in = new AES256v2CipherInputStream(new ByteArrayInputStream(
        data), password.toCharArray());

    try {

      byte[] result = new byte[256];
      IOUtils.readFully(in, result);

      assertArrayEquals(plaintext, result);
    } finally {
      in.close();
    }
  }

  /**
   * Test reading of data with keys.
   * 
   * @throws Exception
   */
  @Test
  public void testUsingKeys() throws Exception {

    byte[] plaintext = getRandomBytes(256);
    byte[] encryptionSalt = getRandomBytes(8);
    byte[] hmacSalt = getRandomBytes(8);

    final String password = "Testing1234";

    JNCryptor cryptor = new AES256v2Cryptor();

    SecretKey hmacKey = cryptor
        .keyForPassword(password.toCharArray(), hmacSalt);
    SecretKey encryptionKey = cryptor.keyForPassword(password.toCharArray(),
        encryptionSalt);

    byte[] data = cryptor.encryptData(plaintext, encryptionKey, hmacKey);

    InputStream in = new AES256v2CipherInputStream(new ByteArrayInputStream(
        data), encryptionKey, hmacKey);

    try {

      byte[] result = new byte[256];
      IOUtils.readFully(in, result);

      assertArrayEquals(plaintext, result);
    } finally {
      in.close();
    }
  }
  
  
  /**
   * Test failure if MAC is broken.
   * 
   * @throws Exception
   */
  @Test(expected = IOException.class) // TODO check for exact message
  public void testBadHMAC() throws Exception {
    byte[] plaintext = getRandomBytes(256);

    final String password = "Testing1234";

    JNCryptor cryptor = new AES256v2Cryptor();
    byte[] data = cryptor.encryptData(plaintext, password.toCharArray());
    System.out.println(DatatypeConverter.printHexBinary(data));
    data[data.length - 1] = (byte) (data[data.length - 1] + 1);
    System.out.println(DatatypeConverter.printHexBinary(data));
    
    InputStream in = new AES256v2CipherInputStream(new ByteArrayInputStream(
        data), password.toCharArray());

    try {

      byte[] result = new byte[256];
      IOUtils.readFully(in, result);

      assertArrayEquals(plaintext, result);
    } finally {
      in.close();
    }
  }  

  private static byte[] getRandomBytes(int length) {
    byte[] result = new byte[length];
    RANDOM.nextBytes(result);
    return result;
  }
}
