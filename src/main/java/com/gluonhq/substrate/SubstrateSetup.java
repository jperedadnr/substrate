/*
 * Copyright (c) 2019, Gluon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
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
package com.gluonhq.substrate;

import com.gluonhq.substrate.model.ProjectConfiguration;
import com.gluonhq.substrate.model.Triplet;
import com.gluonhq.substrate.util.FileDeps;

import java.io.IOException;

public class SubstrateSetup {

    public static void main(String[] args) throws Exception {

        String targetProfile = System.getProperty("targetProfile");

        Triplet targetTriplet = targetProfile != null? new Triplet(Constants.Profile.valueOf(targetProfile.toUpperCase()))
                :Triplet.fromCurrentOS();

        ProjectConfiguration config = new ProjectConfiguration();
        config.setUseJavaFX(true);
        config.setJavaStaticSdkVersion(Constants.DEFAULT_JAVA_STATIC_SDK_VERSION);
        config.setJavafxStaticSdkVersion(Constants.DEFAULT_JAVAFX_STATIC_SDK_VERSION);
        config.setTarget(targetTriplet);
        System.err.println("Config: " + config);

        try {
            System.err.println("Setup Substrate...");
            if (!setup(config)) {
                System.err.println("Setup failed");
                System.exit(1);
            }
        } catch (Throwable t) {
            System.err.println("Setup failed with an exception");
            t.printStackTrace();
            System.exit(1);
        }
    }

    public static boolean setup(ProjectConfiguration config) throws IOException {
        if (!FileDeps.setupDependencies(config)) {
            throw new RuntimeException("Error while setting up dependencies");
        }
        return true;
    }

}
