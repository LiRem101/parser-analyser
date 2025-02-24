/**
 * Inmemantlr - In memory compiler for Antlr 4
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Julian Thome <julian.thome.de@gmail.com>
 *
 * SPDX-FileCopyrightText: 2016 Julian Thome <julian.thome.de@gmail.com>
 *
 * SPDX-License-Identifier: MIT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 **/

package org.snt.inmemantlr.utils;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class FileUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

    private FileUtils() {
    }

    /**
     * load file content
     *
     * @param path path of the file to load
     * @return file content as string
     */
    public static String loadFileContent(File path) {
        return loadFileContent(path.getAbsolutePath());
    }

    /**
     * load file content
     *
     * @param path path of the file to load
     * @return file content as string
     */
    public static String loadFileContent(String path) {
        byte[] bytes;
        try (RandomAccessFile f = new RandomAccessFile(path, "r")) {
            bytes = new byte[(int) f.length()];
            f.read(bytes);
        } catch (IOException e) {
            return null;
        }
        return new String(bytes);
    }

    /**
     * load input stream
     *
     * @param is input stream
     * @return stream content as string
     */
    public static String getStringFromStream(InputStream is) {
        try {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * writes string to file
     * @param string string to write
     * @param file file to which the string shall be written
     * @throws FileExistsException is thrown if file already exists
     */
    public static void writeStringToFile(String string, String file) throws
            FileExistsException {

        LOGGER.debug("file {}", file);

        if(Files.exists(Paths.get(file)))
            throw new FileExistsException("File " + file + " already exists");


        File f = new File(file);

        f.getParentFile().mkdirs();

        try (PrintWriter out = new PrintWriter(f)) {
            out.write(string);
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage());
            assert false;
        }
    }
}
