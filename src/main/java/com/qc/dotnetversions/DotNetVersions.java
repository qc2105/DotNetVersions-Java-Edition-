/*
MIT License

Copyright (c) 2019 José M. Alarcón for the original .NET version
Copyright (c) 2020 ZWSOFT CO., LTD., Chuan Qin for the Java implementation

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package com.qc.dotnetversions;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author localuser
 */
public class DotNetVersions 
{
    private static final Logger LOGGER = Logger.getLogger(DotNetVersions.class.getName());
    
    public static void main(String[] args) 
    {
        //Show all the installed versions
        for (String version : Get1To45VersionFromRegistry()) 
        {
            System.out.println(version);
        }
        for (String version : Get45PlusFromRegistry())
        {
            System.out.println(version);
        }
    }
    
    public static List<String> Get1To45VersionFromRegistry()
    {
        String registryRoot = "SOFTWARE\\Microsoft\\NET Framework Setup\\NDP\\";
        String[] ndpKeys = null;
        try 
        {
            ndpKeys = Advapi32Util.registryGetKeys(WinReg.HKEY_LOCAL_MACHINE, registryRoot);
        }
        catch (Win32Exception e)
        {
            LOGGER.info(e.toString());
        }
        List<String> versions = new ArrayList<>();
        if (ndpKeys.length > 0)
        {
            for (String versionKeyName : ndpKeys)
            {
                // Skip .NET Framework 4.5 version information.
                if ("v4".equals(versionKeyName))
                {
                    continue;
                }
                
                if (versionKeyName.startsWith("v"))
                {                    
                    String versionKey = registryRoot + versionKeyName;
                    // Get the .NET Framework version value.
                    String name = new String();
                    String sp = new String();
                    String install = new String();
                    
                    try 
                    {
                        name = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, versionKey, "Version");
                    }
                    catch (Win32Exception e)
                    {
                        LOGGER.info(e.toString());
                    }
                    try 
                    {
                        // Get the service pack (SP) number.
                        sp = Integer.toString(Advapi32Util.registryGetIntValue(WinReg.HKEY_LOCAL_MACHINE, versionKey, "SP"));
                    }
                    catch (Win32Exception e)
                    {
                        LOGGER.info(e.toString());
                    }
                    try 
                    {
                        // Get the installation flag, or an empty string if there is none.
                        install = Integer.toString(Advapi32Util.registryGetIntValue(WinReg.HKEY_LOCAL_MACHINE, versionKey, "Install"));
                    }
                    catch (Win32Exception e)
                    {
                        LOGGER.info(e.toString());
                    }

                    if (install.isEmpty() && !name.isEmpty())
                    {
                        name = name.trim();
                        versions.add(name);
                    }
                    else
                    {
                        if (!sp.isEmpty() && install.equals("1") && !name.isEmpty())
                        {
                            name = name.trim();
                            name = name + " Service Pack " + install;
                            versions.add(name);
                        }
                    }
                    if (!name.isEmpty())
                    {
                        continue;
                    }
                    String[] subKeyNames = null;
                    try 
                    {
                        subKeyNames = Advapi32Util.registryGetKeys(WinReg.HKEY_LOCAL_MACHINE, versionKey);
                    }
                    catch (Win32Exception e)
                    {
                        LOGGER.info(e.toString());
                    }
                    for (String subKeyName : subKeyNames )
                    {
                        String subVersionKey = versionKey + "\\" + subKeyName;
                        name = "";
                        try 
                        {
                            name = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, subVersionKey, "Version");
                        } 
                        catch (Win32Exception e)
                        {
                            LOGGER.info(e.toString());
                        }
                        try 
                        {
                            if (!name.isEmpty())
                            {
                                sp = Integer.toString(Advapi32Util.registryGetIntValue(WinReg.HKEY_LOCAL_MACHINE, subVersionKey, "SP"));
                            }
                        } 
                        catch (Win32Exception e)
                        {
                            LOGGER.info(e.toString());
                        }
                        try 
                        {
                            install = Integer.toString(Advapi32Util.registryGetIntValue(WinReg.HKEY_LOCAL_MACHINE, subVersionKey, "Install"));
                            if (install.isEmpty() && !name.isEmpty())
                            {
                                versions.add(name);
                            }
                            else
                            {
                                if (!sp.isEmpty() && install.equals("1") && !name.isEmpty())
                                {
                                    name = name.trim();
                                    name = name + " Service Pack " + install;
                                    versions.add(name);
                                }
                                else if (install.equals("1") && !name.isEmpty())
                                {
                                    versions.add(name);
                                }
                            }
                        } 
                        catch (Win32Exception e)
                        {
                            LOGGER.info(e.toString());
                        }
                    }
                } 
            }
        }
        
        return versions;
    }
    
    public static List<String> Get45PlusFromRegistry()
    {
        List<String> versions = new ArrayList<String>();
        String subKey = "SOFTWARE\\Microsoft\\NET Framework Setup\\NDP\\v4\\Full\\";
                
        try 
        {
            String name = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, subKey, "Version");
            if (!name.isEmpty())
            {
                versions.add(name);
            }
            else
            {
                try 
                {
                    int release = Advapi32Util.registryGetIntValue(WinReg.HKEY_LOCAL_MACHINE, subKey, "Release");
                    versions.add(CheckFor45PlusVersion(release));
                }
                catch (Win32Exception e)
                {
                    LOGGER.info(e.toString());
                }
            }
        }
        catch (Win32Exception e)
        {
            LOGGER.info(e.toString());
        }
    
        return versions;
    }
    
    // Checking the version using >= enables forward compatibility.
    private static String CheckFor45PlusVersion(int releaseKey)
    {
        if (releaseKey >= 528040)
            return "4.8";
        if (releaseKey >= 461808)
            return "4.7.2";
        if (releaseKey >= 461308)
            return "4.7.1";
        if (releaseKey >= 460798)
            return "4.7";
        if (releaseKey >= 394802)
            return "4.6.2";
        if (releaseKey >= 394254)
            return "4.6.1";
        if (releaseKey >= 393295)
            return "4.6";
        if (releaseKey >= 379893)
            return "4.5.2";
        if (releaseKey >= 378675)
            return "4.5.1";
        if (releaseKey >= 378389)
            return "4.5";
        // This code should never execute. A non-null release key should mean
        // that 4.5 or later is installed.
        return "unknown";
    }
}
