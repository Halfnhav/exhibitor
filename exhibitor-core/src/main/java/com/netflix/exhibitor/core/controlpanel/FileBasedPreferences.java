/*
 * Copyright 2013 Netflix, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.netflix.exhibitor.core.controlpanel;

import com.google.common.io.Closeables;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

/**
 * A wrapper around the JDK Preferences API that uses a file. IMPORTANT:
 * this class does NOT support multiple processes using the file.
 */
public class FileBasedPreferences extends AbstractPreferences
{
    private final File file;
    private final Properties properties = new Properties();

    /**
     * @param file file to use as backing store
     * @throws IOException errors
     */
    public FileBasedPreferences(File file) throws IOException
    {
        super(null, "");
        this.file = file;

        if ( file.exists() )
        {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
            try
            {
                properties.load(in);
            }
            finally
            {
                Closeables.closeQuietly(in);
            }
        }
    }

    @Override
    protected void putSpi(String key, String value)
    {
        properties.setProperty(key, value);
    }

    @Override
    protected String getSpi(String key)
    {
        return properties.getProperty(key);
    }

    @Override
    protected void removeSpi(String key)
    {
        properties.remove(key);
    }

    @Override
    protected void removeNodeSpi() throws BackingStoreException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    protected String[] keysSpi() throws BackingStoreException
    {
        return new String[0];
    }

    @Override
    protected String[] childrenNamesSpi() throws BackingStoreException
    {
        return new String[0];
    }

    @Override
    protected AbstractPreferences childSpi(String name)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void syncSpi() throws BackingStoreException
    {
    }

    @Override
    protected void flushSpi() throws BackingStoreException
    {
        if ( !file.getParentFile().exists() && !file.getParentFile().mkdirs() )
        {
            throw new BackingStoreException("Could not create parent directories for: " + file);
        }

        OutputStream        out = null;
        try
        {
            out = new BufferedOutputStream(new FileOutputStream(file));
            properties.store(out, "# Auto-generated by " + FileBasedPreferences.class.getName());
        }
        catch ( IOException e )
        {
            throw new BackingStoreException(e);
        }
        finally
        {
            Closeables.closeQuietly(out);
        }
    }
}
