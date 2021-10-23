/*
 * Copyright (c) 2016 - 2021 coodex.org (jujus.shen@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test.org.coodex.jts;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

public class PolygonTest {

    public static void main(String[] args) throws ParseException {
        String p1 = "POLYGON ((13045524.640628718 4521501.126289912, 13045524.269608628 4521501.360472948, 13045524.084097698 4521501.59463334, 13045524.084101211 4521501.126357857, 13045523.713089896 4521500.189852237, 13045523.713103946 4521498.316750676, 13045523.52760531 4521496.911947368, 13045524.084141608 4521495.741191288, 13045524.084152147 4521494.336365686, 13045523.898651758 4521493.165700471, 13045523.713153126 4521491.760897857, 13045523.342145313 4521490.356118068, 13045522.785628319 4521488.9513610955, 13045522.229104282 4521488.483154072, 13045521.487076303 4521487.312557373, 13045520.93055223 4521486.844350386, 13045520.559540858 4521485.9078459935, 13045520.003023788 4521484.503089545, 13045519.632015908 4521483.098310665, 13045520.003041351 4521482.1617161585, 13045520.188557576 4521481.225144341, 13045520.003057158 4521480.05448056, 13045519.632051038 4521478.415565049, 13045519.261044906 4521476.776649786, 13045519.075549742 4521474.903575022, 13045518.890052827 4521473.264637702, 13045517.77700804 4521471.859950684, 13045517.405996613 4521470.923447575, 13045517.591512866 4521469.986876789, 13045517.962538341 4521469.050283492, 13045517.962541856 4521468.5820094645, 13045517.77703965 4521467.645484065, 13045517.962555904 4521466.708913567, 13045517.96256293 4521465.772365744, 13045518.333591904 4521464.367498981, 13045518.704619125 4521463.196769285, 13045519.261157304 4521461.791880253, 13045519.446677055 4521460.387036608, 13045519.261180136 4521458.748101567, 13045519.446696373 4521457.811531854, 13045519.632216118 4521456.4066887405, 13045519.446713932 4521455.470164446, 13045519.261217013 4521453.831230177, 13045519.446736764 4521452.4263876, 13045519.075723609 4521451.72402289, 13045519.261241598 4521450.553317201, 13045519.07574468 4521448.914383699, 13045519.075758727 4521447.041291326, 13045518.704750834 4521445.636517463, 13045518.890272345 4521443.997539534, 13045520.188854374 4521441.656016987, 13045519.261322383 4521439.783038945, 13045519.446847396 4521437.6757892845, 13045519.632367143 4521436.270948878, 13045520.188907051 4521434.631927192, 13045520.374428552 4521432.992950973, 13045520.374437332 4521431.822269984, 13045520.559960578 4521429.949158063, 13045520.559971115 4521428.544341299, 13045521.30201493 4521427.607706426, 13045521.487534663 4521426.202867356, 13045522.044074524 4521424.563847198, 13045522.600614373 4521422.924827283, 13045522.97164327 4521421.519966198, 13045523.52817783 4521420.58335451, 13045523.528186608 4521419.412674911, 13045523.899211982 4521418.476086042, 13045524.270237349 4521417.539497253, 13045524.455753537 4521416.602931186, 13045524.64126622 4521416.1346368585, 13045525.383308135 4521415.432138772, 13045525.383318672 4521414.027323959, 13045525.383327449 4521412.856645093, 13045525.754356302 4521411.451785314, 13045525.939879501 4521409.578676993, 13045525.754384395 4521407.705614325, 13045525.56888402 4521406.534958827, 13045525.38338364 4521405.3643034585, 13045525.568899821 4521404.427738475, 13045525.754419515 4521403.022902481, 13045526.125450116 4521401.383908508, 13045526.496480707 4521399.744914788, 13045526.125474699 4521398.106011981, 13045525.754463412 4521397.1695156265, 13045525.56896479 4521395.764725927, 13045525.19795174 4521395.06236513, 13045525.568980593 4521393.657507744, 13045526.125522107 4521391.784357291, 13045525.940030508 4521389.443027334, 13045525.94004631 4521387.335810426, 13045525.569045559 4521384.994504123, 13045525.94007967 4521382.887242793, 13045525.940093717 4521381.01416225, 13045525.940106008 4521379.375217051, 13045525.940121809 4521377.268002172, 13045525.940135855 4521375.394922636, 13045525.94014639 4521373.990113203, 13045525.569142126 4521372.117079573, 13045526.31118928 4521370.712179942, 13045527.05323816 4521369.073145688, 13045527.23875784 4521367.668314435, 13045527.79529404 4521366.497572737, 13045528.537341122 4521365.09267378, 13045528.722860783 4521363.68784305, 13045529.279396944 4521362.517101757, 13045529.279407479 4521361.112294056, 13045529.27941977 4521359.47335198, 13045529.65045031 4521357.834364774, 13045530.206989959 4521356.19535512, 13045530.021496642 4521354.0881676115, 13045529.835996298 4521352.917518154, 13045529.464986833 4521351.74689152, 13045528.908468233 4521350.576287695, 13045528.722966122 4521349.639772963, 13045528.90848052 4521348.937347268, 13045529.465009654 4521348.703144874, 13045530.021540534 4521348.234808156, 13045530.763584021 4521347.298180149, 13045531.320118373 4521346.36157491, 13045531.691140098 4521345.893260959, 13045532.247672675 4521345.190790062, 13045533.175223418 4521344.488273749, 13045534.47378878 4521344.253980416, 13045536.328879505 4521344.253752978, 13045538.18397183 4521344.019391154, 13045540.224574776 4521343.550872178, 13045542.079668516 4521343.082375827, 13045543.934762098 4521342.613879335, 13045545.604343023 4521342.6136738965, 13045547.459432783 4521342.613445478, 13045549.129009921 4521343.081508145, 13045550.798590438 4521343.0813022945, 13045552.468169073 4521343.315230511, 13045553.952240422 4521343.315047306, 13045555.621818807 4521343.548975279, 13045557.105889935 4521343.548791849, 13045558.589960963 4521343.548608319, 13045560.259542499 4521343.314267514, 13045561.929120397 4521343.548194986, 13045563.784208747 4521343.547965131, 13045565.453788122 4521343.547758119, 13045566.752349772 4521343.547597019, 13045568.421928918 4521343.547389775, 13045570.09150969 4521343.313048192, 13045571.575581577 4521343.078729548, 13045573.059649846 4521343.312679195, 13045574.543718008 4521343.546628743, 13045576.027786074 4521343.780578194, 13045577.511854034 4521344.014527545, 13045579.181428837 4521344.482587894, 13045580.665498335 4521344.482402809, 13045582.520583307 4521344.716305539, 13045584.375666367 4521345.18434235, 13045586.045245899 4521344.949999471, 13045587.529314924 4521344.949813901, 13045589.38440282 4521344.715447559, 13045591.239490556 4521344.481081058, 13045593.09457286 4521344.949117096, 13045594.764146453 4521345.417176255, 13045596.619230205 4521345.651077767, 13045598.474315561 4521345.650844865, 13045599.95838197 4521345.884792681, 13045601.442450043 4521345.884606137, 13045602.926519763 4521345.650285233, 13045604.225073874 4521346.352524616, 13045605.709138134 4521346.820606325, 13045607.378712505 4521347.054530283, 13045608.862780059 4521347.054343221, 13045610.346847508 4521347.054156054, 13045612.016419765 4521347.522213947, 13045613.871496767 4521348.45851692, 13045615.726571852 4521349.6289541535, 13045616.83962034 4521349.862947772, 13045617.952670528 4521349.862806987, 13045618.880212305 4521349.862689621, 13045620.178768976 4521350.096659592, 13045621.29181899 4521350.096518633, 13045622.590379026 4521349.862219754, 13045624.07444729 4521349.627897281, 13045625.373005401 4521349.627732583, 13045626.300548617 4521349.393480552, 13045627.042583514 4521349.159252037, 13045627.970126657 4521348.924999944, 13045628.712157987 4521349.159040035, 13045628.712154474 4521349.62730871, 13045628.712149203 4521350.329711761, 13045629.083165731 4521350.329664631, 13045630.010707034 4521350.3295467775, 13045631.123754788 4521350.563539665, 13045632.236802481 4521350.797532497, 13045633.720866568 4521351.031478066, 13045635.204928799 4521351.499557918, 13045636.874500884 4521351.733479662, 13045638.35856641 4521351.7332905345, 13045639.84263008 4521351.967235695, 13045641.512201805 4521352.201157085, 13045643.181775156 4521352.200943945, 13045645.222362876 4521352.434817666, 13045647.077444054 4521352.434580511, 13045649.11803492 4521352.200185052, 13045651.344128402 4521352.668168852, 13045653.19920554 4521353.136199998, 13045654.683266433 4521353.604278582, 13045656.167328984 4521353.838222646, 13045657.280377146 4521353.838079801, 13045658.393425252 4521353.837936899, 13045659.135457287 4521353.837841596, 13045659.691979548 4521354.071904542, 13045660.062995544 4521354.071856872, 13045660.805029282 4521353.837627073, 13045661.547061238 4521353.837531689, 13045662.474602902 4521353.603277988, 13045663.402141014 4521353.837293113, 13045664.515190568 4521353.603015454, 13045665.813746247 4521353.602848286, 13045666.92679216 4521353.836839372, 13045667.483319486 4521353.368498814, 13045668.596367065 4521353.368355376, 13045669.70941459 4521353.36821188, 13045670.822462056 4521353.368068329, 13045672.121019121 4521353.133766352, 13045673.605080476 4521353.367709193, 13045674.90363562 4521353.3675414715, 13045676.387698539 4521353.367349694, 13045677.686251752 4521353.601316231, 13045678.613790974 4521353.601196259, 13045679.355825834 4521353.132831404, 13045680.283366736 4521352.898576942, 13045681.39641542 4521352.664298425, 13045682.509462284 4521352.664154263, 13045683.99352469 4521352.663961955, 13045685.292077448 4521352.897928016, 13045686.77613966 4521352.897735516, 13045688.07469577 4521352.66343258, 13045689.744262025 4521353.131484624, 13045691.0428162 4521353.131315921, 13045692.341370296 4521353.131147139, 13045694.196445687 4521353.365040307, 13045695.866013227 4521353.598957472, 13045697.164567038 4521353.598788396, 13045698.46312077 4521353.598619242, 13045699.947182082 4521353.598425828, 13045701.431245051 4521353.364097881, 13045702.915304404 4521353.598038688, 13045704.399363657 4521353.831979397, 13045705.883422805 4521354.065920009, 13045707.7384988 4521354.065677581, 13045709.779083971 4521353.831276286, 13045711.819668945 4521353.5968748, 13045713.674744433 4521353.596631858, 13045715.529816246 4521354.064657627, 13045717.19938215 4521354.298573141, 13045718.86894793 4521354.53248853, 13045720.538515335 4521354.532269337, 13045722.2080791 4521355.0003189305, 13045724.063150192 4521355.468344023, 13045725.918221116 4521355.936368977, 13045727.587789744 4521355.702014756, 13045729.442865647 4521355.467635973, 13045731.112430487 4521355.701550434, 13045732.967504332 4521355.701305822, 13045734.822576268 4521355.93519553, 13045736.492142454 4521355.934975097, 13045738.347214086 4521356.168864505, 13045740.387794632 4521356.168594735, 13045742.242864178 4521356.636618307, 13045744.097933564 4521357.104641741, 13045745.953006309 4521357.104396005, 13045747.993586147 4521357.104125514, 13045749.66315133 4521357.103904058, 13045751.332716381 4521357.103682472, 13045753.002279548 4521357.33759527, 13045754.486335393 4521357.571532597, 13045755.78488396 4521357.805494494, 13045757.454448547 4521357.805272434, 13045759.124013003 4521357.805050245, 13045760.79357558 4521358.038962449, 13045762.277632656 4521358.038764724, 13045763.94719674 4521358.038542158, 13045765.616760708 4521358.038319465, 13045767.100815702 4521358.272255938, 13045768.399365272 4521358.272082551, 13045769.512400672 4521359.208472054, 13045770.25442014 4521360.379045746, 13045770.625428986 4521361.081399931, 13045770.254414866 4521361.081449518, 13045769.88340426 4521360.613229911, 13045770.439925447 4521360.61315554, 13045770.996444857 4521360.847215746, 13045771.923976596 4521361.3153609345, 13045772.666004766 4521361.3152617, 13045773.222524127 4521361.549321864, 13045774.150061041 4521361.315063155, 13045774.892090896 4521361.080829244, 13045775.634118972 4521361.080729906, 13045776.747161033 4521361.080580856, 13045777.860204797 4521360.846297151, 13045778.97324851 4521360.612013393, 13045780.08629216 4521360.377729584, 13045781.384840963 4521360.377555417, 13045782.312379297 4521359.909161809, 13045782.868903678 4521359.440817988, 13045782.49788801 4521359.675002345, 13045781.570353204 4521359.675126807, 13045780.271802653 4521359.9094355535, 13045778.602239804 4521359.909659385, 13045777.11818383 4521359.909858236, 13045775.4486225 4521359.675947252, 13045774.15007686 4521359.207851942, 13045773.037036423 4521358.973866305, 13045771.923995927 4521358.739880619, 13045770.625446564 4521358.740054216, 13045769.883416565 4521358.974287929, 13045769.51239716 4521359.676741176, 13045769.512391886 4521360.379144899, 13045769.326879542 4521361.081573451, 13045769.326870754 4521362.2522465065, 13045769.326863723 4521363.188785044, 13045768.955839042 4521364.593642571, 13045768.955830252 4521365.7643160205, 13045768.770319663 4521366.232610214, 13045768.584805558 4521367.169173884, 13045768.39929145 4521368.10573764, 13045767.842759669 4521369.510620582, 13045767.657240286 4521371.149588988, 13045767.471722655 4521372.554422788, 13045767.657220952 4521373.725072359, 13045767.657208646 4521375.364016654, 13045767.286180416 4521377.2371457, 13045767.28616987 4521378.641955549, 13045767.100655753 4521379.578520322, 13045766.544123938 4521380.983404772, 13045766.173099207 4521382.388264643, 13045765.987586832 4521383.090694648, 13045765.987579802 4521384.0272350535, 13045765.802063916 4521385.197935431, 13045765.616553305 4521385.666230468, 13045765.245530311 4521386.836955776, 13045765.245519765 4521388.2417669175, 13045765.245510975 4521389.412443015, 13045765.43100753 4521390.817229753, 13045765.24548988 4521392.222066182, 13045765.059973987 4521393.392767468, 13045765.245472303 4521394.563419394, 13045765.245461758 4521395.968231577, 13045765.059944108 4521397.373068693, 13045765.245440664 4521398.777856504, 13045765.801953174 4521399.948459527, 13045765.801939115 4521401.821543441, 13045765.801926808 4521403.4604921425, 13045766.172933973 4521404.396984863, 13045766.172921665 4521406.035933966, 13045765.987400504 4521407.909043727, 13045765.801882861 4521409.313882454, 13045765.801872315 4521410.7186966175, 13045766.172875963 4521412.123461459, 13045765.987358317 4521413.528300754, 13045765.616337089 4521414.464893395, 13045765.616328299 4521415.635572426, 13045765.43081241 4521416.806276341, 13045765.245296521 4521417.976980386, 13045765.245282454 4521419.85006753, 13045765.059761284 4521421.7231797585, 13045764.874241875 4521423.362156324, 13045764.68872774 4521424.298725083, 13045764.132195862 4521425.703615483, 13045763.946671167 4521428.045000936, 13045763.761149991 4521429.918114619, 13045763.575623538 4521432.493637207, 13045763.3901006 4521434.600887929, 13045763.019070536 4521436.7081638025, 13045763.019059988 4521438.112981646, 13045763.019045925 4521439.986072401, 13045763.01903186 4521441.859163488, 13045763.019016035 4521443.966391365, 13045763.204509092 4521445.839458437, 13045763.204496786 4521447.478414036, 13045762.83346848 4521449.351555927, 13045762.462440165 4521451.224698147, 13045762.276922492 4521452.629542667, 13045761.90589768 4521454.034412094, 13045761.349365737 4521455.439306419, 13045761.16384805 4521456.844151494, 13045760.978332119 4521458.014859986, 13045760.97832509 4521458.951407106, 13045761.349328812 4521460.356178511, 13045760.978305751 4521461.526912111, 13045760.97829696 4521462.697596414, 13045760.79278455 4521463.400031769, 13045760.978284653 4521464.336554659, 13045761.163782999 4521465.507214564, 13045761.349281346 4521466.6778746, 13045761.534774415 4521468.550945753, 13045761.720265722 4521470.658154291, 13045761.349239148 4521472.297163219, 13045761.534733977 4521473.936098252, 13045761.7202288 4521475.575033541, 13045761.905721867 4521477.448106288, 13045761.90570956 4521479.087066846, 13045761.53468123 4521480.960214377, 13045761.349154755 4521483.53574952, 13045760.978124656 4521485.6430352265, 13045760.607099826 4521487.04790907, 13045760.236074992 4521488.452783094, 13045760.050559055 4521489.623495118, 13045759.865043115 4521490.794207272, 13045759.865025535 4521493.135582565, 13045760.050518613 4521495.008658472, 13045759.864997402 4521496.881784123, 13045759.864983333 4521498.754885407, 13045759.864965752 4521501.096262485, 13045759.493932113 4521503.671827276, 13045758.751889426 4521505.545028562, 13045758.195357397 4521506.949929732, 13045757.082305606 4521508.120767225, 13045755.412737487 4521508.589265067, 13045754.114183633 4521509.057713493, 13045752.630126012 4521509.057910671, 13045750.960561067 4521509.058132374, 13045749.290994236 4521509.292491859, 13045747.621425522 4521509.760989147, 13045745.951860195 4521509.761210461, 13045743.911280174 4521509.761480778, 13045742.056203753 4521510.23000222, 13045740.015619857 4521510.698548056, 13045738.53155776 4521511.167020168, 13045737.047495566 4521511.6354922, 13045735.56343503 4521511.869826171, 13045734.264885273 4521511.635859612, 13045732.595320681 4521511.40194192, 13045730.925755966 4521511.168024101, 13045729.256192878 4521510.699968209, 13045727.586629666 4521510.231912204, 13045725.917066325 4521509.7638560925, 13045724.2475011 4521509.5299377935, 13045722.206920814 4521509.296068125, 13045720.166338574 4521509.296336184, 13045718.125757907 4521509.062466136, 13045716.270684568 4521508.82857157, 13045714.230101777 4521508.828839065, 13045712.189520556 4521508.59496846, 13045710.33444495 4521508.595211291, 13045708.108355774 4521508.361364576, 13045706.253279822 4521508.361607052, 13045704.212696096 4521508.361873594, 13045702.172112174 4521508.3621399375, 13045700.131529817 4521508.1282681925, 13045698.090947274 4521507.8943962585, 13045696.23587047 4521507.894637868, 13045694.380791746 4521508.129017206, 13045692.525716383 4521507.895120603, 13045690.485133125 4521507.661247948, 13045688.630053913 4521507.8956267815, 13045686.77497806 4521507.661729682, 13045684.734392501 4521507.661994364, 13045682.879316315 4521507.428096934, 13045681.02424173 4521506.96006148, 13045679.169166978 4521506.492025885, 13045677.499596411 4521506.49224186, 13045675.4590135 4521506.024229952, 13045673.603934763 4521506.0244695945, 13045671.748854108 4521506.258846925, 13045669.893775055 4521506.259086245, 13045668.224202013 4521506.493439346, 13045666.554630598 4521506.493654465, 13045664.699552856 4521506.259755481, 13045662.658968743 4521505.791742358, 13045660.989396896 4521505.791957041, 13045659.134316921 4521505.792195425, 13045657.09373228 4521505.3241817895, 13045655.238655487 4521504.856144185, 13045653.198068723 4521504.6222680295, 13045651.342991596 4521504.154230117, 13045649.302404463 4521503.920353601, 13045647.447323488 4521503.920590966, 13045645.406735986 4521503.686714086, 13045643.180641908 4521503.218722919, 13045641.140054008 4521502.984845643, 13045639.284974087 4521502.75094452, 13045637.244389331 4521502.048791358, 13045635.389310839 4521501.580752168, 13045633.348723955 4521501.112736441, 13045631.308133364 4521501.112996003, 13045629.267544344 4521500.87911763, 13045627.41245989 4521501.113490979, 13045625.3718705 4521500.879612237, 13045623.516785711 4521501.113985244, 13045621.847210845 4521501.114196853, 13045620.36314595 4521500.880247105, 13045618.693570845 4521500.880458465, 13045616.652980626 4521500.646578888, 13045614.797898605 4521500.412675678, 13045613.128323065 4521500.412886602, 13045611.644257572 4521500.178936259, 13045609.78917336 4521500.179170333, 13045608.119597435 4521500.179380862, 13045606.26451468 4521499.945476912, 13045604.409431757 4521499.711572808, 13045602.925363906 4521499.711759572, 13045601.255789204 4521499.477831856, 13045599.400707614 4521499.009789633, 13045597.545622343 4521499.010022634, 13045595.50503013 4521498.7761410605, 13045593.464437718 4521498.542259296, 13045591.23833652 4521498.308400576, 13045589.383252308 4521498.074495192, 13045587.528166177 4521498.0747273145, 13045585.858586771 4521498.309073756, 13045584.00349858 4521498.543443244, 13045582.148411987 4521498.543674895, 13045580.293323476 4521498.778044064, 13045578.438234806 4521499.012413074, 13045576.58314949 4521498.77850655, 13045574.728064017 4521498.5445998665, 13045572.687467871 4521498.5448537795, 13045570.646873292 4521498.310969814, 13045568.606276764 4521498.31122333, 13045566.751190597 4521498.077315957, 13045564.710593699 4521498.077569095, 13045562.66999309 4521498.546097384, 13045561.00040999 4521499.014579557, 13045559.145321408 4521499.014809187, 13045557.290232668 4521499.015038651, 13045555.249634871 4521499.015290876, 13045553.20903687 4521499.015542901, 13045551.353947619 4521499.015771844, 13045549.313351009 4521498.781885805, 13045547.458261415 4521498.782114403, 13045545.603169907 4521499.016480529, 13045543.933588995 4521499.016685981, 13045542.26400795 4521499.016891302, 13045540.594428528 4521498.782958803, 13045538.924847223 4521498.783163856, 13045537.255265785 4521498.783368779, 13045535.214669593 4521498.315343703, 13045533.359580534 4521498.081433389, 13045531.690002171 4521497.613362544, 13045529.83491281 4521497.379451932, 13045528.165332422 4521497.145518484, 13045526.495750144 4521497.145722552, 13045525.197184308 4521497.38001883, 13045523.527600039 4521497.614360319, 13045522.043526592 4521497.614541393, 13045520.930471437 4521497.614677127, 13045520.002921918 4521498.083065524, 13045519.446394296 4521498.083133346, 13045519.44639254 4521498.317271017, 13045524.640628718 4521501.126289912))";
        String p2 = "POLYGON ((13045712.418896468 4521363.085914767, 13045703.739858795 4521504.923035285, 13045701.457102036 4521504.627986636, 13045709.50709315 4521364.213442394, 13045706.16344976 4521360.747556194, 13045703.401125379 4521363.101882733, 13045695.974666748 4521504.088562583, 13045686.959987085 4521503.201596335, 13045688.049511245 4521499.2478948645, 13045690.880824188 4521499.766498044, 13045692.252268894 4521497.676249394, 13045699.781532746 4521360.001693359, 13045696.424216388 4521358.893349156, 13045694.990852404 4521362.067511838, 13045692.77715256 4521360.174668316, 13045690.153847761 4521362.544957735, 13045682.661518466 4521501.917305999, 13045685.685059031 4521410.571533164, 13045689.162595682 4521357.304363023, 13045696.522320127 4521357.138204894, 13045697.515479779 4521353.745030289, 13045695.379659254 4521352.445695578, 13045687.999733176 4521352.6113901865, 13045680.1318907 4521360.564257314, 13045680.46682719 4521366.02867396, 13045683.73901477 4521366.982020959, 13045683.503731174 4521371.789000181, 13045680.581120225 4521374.050370365, 13045674.792506704 4521476.882755438, 13045675.918848379 4521479.114650246, 13045678.241900437 4521479.291229037, 13045677.103445385 4521502.550520635, 13045675.579805482 4521504.593957145, 13045677.822995335 4521507.326739478, 13045695.809028784 4521509.096409082, 13045696.854463356 4521511.149541823, 13045700.49629744 4521509.55759564, 13045705.847363675 4521511.26703346, 13045712.939302402 4521509.525843386, 13045712.363034168 4521506.037589535, 13045708.721874952 4521505.566965977, 13045717.631952332 4521368.030867335, 13045717.410189586 4521363.380860551, 13045715.062015919 4521360.737741101, 13045712.418896468 4521363.085914767))";
        WKTReader wktReader = new WKTReader();
        Geometry geometry1 = wktReader.read(p1);
        Geometry geometry2 = wktReader.read(p2);
//        geometry1 = geometry1.intersection(geometry1);
//        geometry1 = geometry1.buffer(0);
//        IsValidOp isValidOp = new IsValidOp(geometry1);
//        if(!isValidOp.isValid()) {
//            geometry1 = geometry1.buffer(0);
//        }
        if (!geometry1.isValid()) {
            geometry1 = geometry1.buffer(0);
        }
        System.out.println(geometry2.intersection(geometry1));

    }
}
