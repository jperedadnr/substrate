/*
 * Copyright (c) 2022, 2024, Gluon
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
#include <stdlib.h>
#include <stdio.h>
#include <stdint.h>

typedef struct {
#ifdef GVM_IOS_SIM
  char fCX8;
  char fCMOV;
  char fFXSR;
  char fHT;
  char fMMX;
  char fAMD3DNOWPREFETCH;
  char fSSE;
  char fSSE2;
  char fSSE3;
  char fSSSE3;
  char fSSE4A;
  char fSSE41;
  char fSSE42;
  char fPOPCNT;
  char fLZCNT;
  char fTSC;
  char fTSCINV;
  char fAVX;
  char fAVX2;
  char fAES;
  char fERMS;
  char fCLMUL;
  char fBMI1;
  char fBMI2;
  char fRTM;
  char fADX;
  char fAVX512F;
  char fAVX512DQ;
  char fAVX512PF;
  char fAVX512ER;
  char fAVX512CD;
  char fAVX512BW;
  char fAVX512VL;
  char fSHA;
  char fFMA;
#else
  char fFP;
  char fASIMD;
  char fEVTSTRM;
  char fAES;
  char fPMULL;
  char fSHA1;
  char fSHA2;
  char fCRC32;
  char fLSE;
  char fSTXRPREFETCH;
  char fA53MAC;
  char fDMBATOMICS;
#endif
} CPUFeatures;

void determineCPUFeatures(CPUFeatures* features)
{
    fprintf(stderr, "\n\n\ndetermineCpuFeaures\n");
#ifdef GVM_IOS_SIM
    features->fSSE = 1;
    features->fSSE2 = 1;
#else
    features->fFP = 1;
    features->fASIMD = 1;
#endif
}

// dummy symbols for JDK21+
int checkCPUFeatures(uint8_t *buildtimeFeaturesPtr) {
   return 0;
}
void checkCPUFeaturesOrExit(uint8_t *buildtimeFeaturesPtr, const char *errorMessage) {}
void fprintfSD() {}

// dummy symbols for JDK17+
void Java_java_net_AbstractPlainDatagramSocketImpl_isReusePortAvailable0() {}
void Java_java_net_AbstractPlainSocketImpl_isReusePortAvailable0() {}
void Java_java_net_DatagramPacket_init() {}