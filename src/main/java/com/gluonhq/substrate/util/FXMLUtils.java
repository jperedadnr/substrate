/*
 * Copyright (c) 2020, Gluon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GLUON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gluonhq.substrate.util;

import com.oracle.javafx.scenebuilder.kit.fxom.FXOMDocument;
import com.oracle.javafx.scenebuilder.kit.fxom.FXOMObject;
import com.oracle.javafx.scenebuilder.kit.fxom.FXOMProperty;
import com.oracle.javafx.scenebuilder.kit.metadata.util.PropertyName;
import javafx.fxml.FXMLLoader;
import javafx.application.Platform;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FXMLUtils {

    private static final String FXML_EXTENSION = ".fxml";

    private static final List<String> controllers = new ArrayList<>();
    private static final List<String> imports = new ArrayList<>();
    private static final List<Method> methods = new ArrayList<>();
    private static TransientClassLoader myClassLoader;

    public static void resolveFXMLconfig(Path agentPath, List<File> jars) throws IOException {
        controllers.clear();
        imports.clear();
        methods.clear();

        myClassLoader = new TransientClassLoader(new URL[0], FXMLLoader.getDefaultClassLoader());

        Map<String, String> fxmlMap = scanJars(jars);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(() -> {
            for (String fxmlName : fxmlMap.keySet()) {
                System.out.println("fxml = " + fxmlName);
                try {
                    scanFXMLFile(agentPath, fxmlMap.get(fxmlName));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            latch.countDown();
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Platform.exit();
    }

    private static Map<String, String> scanJars(List<File> jars) throws IOException {
        Map<String, String> map = new HashMap<>();
        for (File jar : jars) {
            try (ZipFile zip = new ZipFile(jar)) {
                Logger.logDebug("Scanning " + jar);
                for (Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements(); ) {
                    ZipEntry zipEntry = e.nextElement();
                    String name = zipEntry.getName();
                    if (!zipEntry.isDirectory() && name.endsWith(FXML_EXTENSION)) {
                        Logger.logDebug("Processing fxml from " + zip.getName() + "::" + name);
                        map.put(name, String.join("\n", FileOps.readFileLines(zip.getInputStream(zipEntry))));
                    }
                }
            }
        }
        return map;
    }
    public static List<Method> getMethods() {
        return methods.stream()
                .distinct()
                .sorted(Comparator.comparing(Method::getName))
                .collect(Collectors.toList());
    }

    public static List<String> getControllers() {
        return controllers.stream()
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public static List<String> getImports() {
        return imports.stream()
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private static void scanFXMLFile(Path location, String fxmlString) throws IOException {
        FXOMDocument fxomDocument = new FXOMDocument(fxmlString, location.toFile().toURI().toURL(), myClassLoader, null);
        FXOMObject root = fxomDocument.getFxomRoot();
        imports.addAll(root.collectDeclaredClasses().stream()
                .map(Class::getCanonicalName)
                .collect(Collectors.toList()));

        imports.addAll(findPropertyClasses(root.getChildObjects().toArray(FXOMObject[]::new)));
        imports.addAll(findPropertyClasses(root));

        controllers.add(root.getFxController());

        findAllMethods(Collections.singletonList(root));
    }

    private static void findAllMethods(List<FXOMObject> objs) {
        methods.addAll(objs.stream()
                .flatMap(fxomObject -> findMethodsForObject(fxomObject).stream())
                .collect(Collectors.toList()));

        objs.forEach(o -> findAllMethods(o.getChildObjects()));
    }

    private static List<Method> findMethodsForObject(FXOMObject o) {
        List<PropertyName> properties = o.collectPropertiesT().stream()
                .map(FXOMProperty::getName)
                .collect(Collectors.toList());

        // static methods
        List<Method> methods = properties.stream()
                .filter(p -> p.getResidenceClass() != null)
                .flatMap(p -> Stream.of(p.getResidenceClass().getMethods())
                        .filter(m -> m.toString().toLowerCase(Locale.ROOT).contains(p.getName().toLowerCase(Locale.ROOT))))
                .collect(Collectors.toList());

        // non-static methods
        final BeanPropertyIntrospector bpi = new BeanPropertyIntrospector(o.getSceneGraphObject());
        methods.addAll(properties.stream()
                .filter(p -> p.getResidenceClass() == null)
                .map(PropertyName::getName)
                .filter(Objects::nonNull)
                .flatMap(n -> bpi.getMethodsForProperty(n).stream())
                .distinct()
                .collect(Collectors.toList()));
        return methods;
    }

    private static Set<String> findPropertyClasses(FXOMObject... fxomObjects) {
        return Arrays.stream(fxomObjects)
                .map(FXOMObject::collectPropertiesT) //list of lists containing FXOMProperties
                .flatMap(List::stream) // add all to one list of FXOMProperties
                .map(FXOMProperty::getName) // list of all PropertyNames
                .filter(prop -> prop.getResidenceClass() != null) // filter for ResidenceClass (used for static methods example: HBox.hgrow="..")
                .map(prop -> prop.getResidenceClass().getName()) // list of classes
                .collect(Collectors.toSet());
    }

    static class TransientClassLoader extends URLClassLoader {

        TransientClassLoader(URL[] urls, ClassLoader parentClassLoader) {
            super(urls, parentClassLoader);
        }

        @Override
        public void addURL(URL url) {
            super.addURL(url);
        }

        @Override
        public URL getResource(String name) {
            URL  result = super.getResource(name);
            if (result == null) {
                try {
                    result = new URL("file", null, name); //NOI18N
                } catch(MalformedURLException x) {
                    throw new RuntimeException("Bug", x); //NOI18N
                }
            }
            return result;
        }
    }

    static class BeanPropertyIntrospector {

        private final PropertyDescriptor[] propertyDescriptors;

        BeanPropertyIntrospector(Object object) {
            Objects.requireNonNull(object);
            try {
                final BeanInfo beanInfo = Introspector.getBeanInfo(object.getClass());
                this.propertyDescriptors = beanInfo.getPropertyDescriptors();
            } catch(IntrospectionException x) {
                throw new RuntimeException(x);
            }
        }

        List<Method> getMethodsForProperty(String propertyName) {
            final PropertyDescriptor d = findDescriptor(propertyName);
            List<Method> result = new ArrayList<>();
            if (d != null) {
                Method readMethod = d.getReadMethod();
                if (readMethod != null) {
                    result.add(readMethod);
                }
                Method writeMethod = d.getWriteMethod();
                if (writeMethod != null) {
                    result.add(writeMethod);
                }
            }
            return result;
        }

        private PropertyDescriptor findDescriptor(String propertyName) {
            int i = 0;
            while (i < propertyDescriptors.length
                    && !propertyDescriptors[i].getName().equals(propertyName)) {
                i++;
            }
            return i < propertyDescriptors.length ? propertyDescriptors[i] : null;
        }
    }
}
