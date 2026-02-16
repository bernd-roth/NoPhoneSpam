/*
 * Copyright © Ricki Hirner (bitfire web engineering).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.bitfire.nophonespam;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Comparator;

public class CountryCode {

    public final String flag;
    public final String name;
    public final String dialCode;

    public CountryCode(String flag, String name, String dialCode) {
        this.flag = flag;
        this.name = name;
        this.dialCode = dialCode;
    }

    @Override
    public String toString() {
        if (dialCode.isEmpty())
            return name;
        return flag + " " + name + " (+" + dialCode + ")";
    }

    /**
     * Find the best-matching country code for a phone number.
     * Tries longest dial codes first so e.g. "+1684" matches American Samoa before "+1" matches US.
     * Returns the index into COUNTRIES, or 0 (None) if no match found.
     */
    public static int findByDialCode(String number) {
        if (number == null || !number.startsWith("+"))
            return 0;

        String digits = number.substring(1); // strip leading "+"

        // Sort a copy of indices by dial code length descending, so longer codes match first
        Integer[] indices = new Integer[COUNTRIES.length];
        for (int i = 0; i < indices.length; i++) indices[i] = i;
        Arrays.sort(indices, new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                return COUNTRIES[b].dialCode.length() - COUNTRIES[a].dialCode.length();
            }
        });

        for (int idx : indices) {
            String code = COUNTRIES[idx].dialCode;
            if (!code.isEmpty() && digits.startsWith(code))
                return idx;
        }
        return 0;
    }

    /**
     * Given a full number and a matched country index, return the local part
     * (the number with the country dial code prefix stripped).
     */
    public static String stripDialCode(String number, int countryIndex) {
        if (countryIndex <= 0 || number == null)
            return number != null ? number : "";
        String code = "+" + COUNTRIES[countryIndex].dialCode;
        if (number.startsWith(code))
            return number.substring(code.length());
        return number;
    }

    public static class CountryCodeAdapter extends ArrayAdapter<CountryCode> {

        public CountryCodeAdapter(Context context) {
            super(context, R.layout.country_code_item, R.id.country_text, COUNTRIES);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return createView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return createView(position, convertView, parent);
        }

        private View createView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.country_code_item, parent, false);
            }
            CountryCode country = getItem(position);
            TextView tv = (TextView) convertView.findViewById(R.id.country_text);
            tv.setText(country.toString());
            return convertView;
        }
    }

    public static final CountryCode[] COUNTRIES = {
        new CountryCode("", "None", ""),
        new CountryCode("\uD83C\uDDE6\uD83C\uDDEB", "Afghanistan", "93"),
        new CountryCode("\uD83C\uDDE6\uD83C\uDDF1", "Albania", "355"),
        new CountryCode("\uD83C\uDDE9\uD83C\uDDFF", "Algeria", "213"),
        new CountryCode("\uD83C\uDDE6\uD83C\uDDF8", "American Samoa", "1684"),
        new CountryCode("\uD83C\uDDE6\uD83C\uDDE9", "Andorra", "376"),
        new CountryCode("\uD83C\uDDE6\uD83C\uDDF4", "Angola", "244"),
        new CountryCode("\uD83C\uDDE6\uD83C\uDDEE", "Anguilla", "1264"),
        new CountryCode("\uD83C\uDDE6\uD83C\uDDEC", "Antigua and Barbuda", "1268"),
        new CountryCode("\uD83C\uDDE6\uD83C\uDDF7", "Argentina", "54"),
        new CountryCode("\uD83C\uDDE6\uD83C\uDDF2", "Armenia", "374"),
        new CountryCode("\uD83C\uDDE6\uD83C\uDDFC", "Aruba", "297"),
        new CountryCode("\uD83C\uDDE6\uD83C\uDDFA", "Australia", "61"),
        new CountryCode("\uD83C\uDDE6\uD83C\uDDF9", "Austria", "43"),
        new CountryCode("\uD83C\uDDE6\uD83C\uDDFF", "Azerbaijan", "994"),
        new CountryCode("\uD83C\uDDE7\uD83C\uDDF8", "Bahamas", "1242"),
        new CountryCode("\uD83C\uDDE7\uD83C\uDDED", "Bahrain", "973"),
        new CountryCode("\uD83C\uDDE7\uD83C\uDDE9", "Bangladesh", "880"),
        new CountryCode("\uD83C\uDDE7\uD83C\uDDE7", "Barbados", "1246"),
        new CountryCode("\uD83C\uDDE7\uD83C\uDDFE", "Belarus", "375"),
        new CountryCode("\uD83C\uDDE7\uD83C\uDDEA", "Belgium", "32"),
        new CountryCode("\uD83C\uDDE7\uD83C\uDDFF", "Belize", "501"),
        new CountryCode("\uD83C\uDDE7\uD83C\uDDEF", "Benin", "229"),
        new CountryCode("\uD83C\uDDE7\uD83C\uDDF2", "Bermuda", "1441"),
        new CountryCode("\uD83C\uDDE7\uD83C\uDDF9", "Bhutan", "975"),
        new CountryCode("\uD83C\uDDE7\uD83C\uDDF4", "Bolivia", "591"),
        new CountryCode("\uD83C\uDDE7\uD83C\uDDE6", "Bosnia and Herzegovina", "387"),
        new CountryCode("\uD83C\uDDE7\uD83C\uDDFC", "Botswana", "267"),
        new CountryCode("\uD83C\uDDE7\uD83C\uDDF7", "Brazil", "55"),
        new CountryCode("\uD83C\uDDE7\uD83C\uDDF3", "Brunei", "673"),
        new CountryCode("\uD83C\uDDE7\uD83C\uDDEC", "Bulgaria", "359"),
        new CountryCode("\uD83C\uDDE7\uD83C\uDDEB", "Burkina Faso", "226"),
        new CountryCode("\uD83C\uDDE7\uD83C\uDDEE", "Burundi", "257"),
        new CountryCode("\uD83C\uDDF0\uD83C\uDDED", "Cambodia", "855"),
        new CountryCode("\uD83C\uDDE8\uD83C\uDDF2", "Cameroon", "237"),
        new CountryCode("\uD83C\uDDE8\uD83C\uDDE6", "Canada", "1"),
        new CountryCode("\uD83C\uDDE8\uD83C\uDDFB", "Cape Verde", "238"),
        new CountryCode("\uD83C\uDDF0\uD83C\uDDFE", "Cayman Islands", "1345"),
        new CountryCode("\uD83C\uDDE8\uD83C\uDDEB", "Central African Republic", "236"),
        new CountryCode("\uD83C\uDDF9\uD83C\uDDE9", "Chad", "235"),
        new CountryCode("\uD83C\uDDE8\uD83C\uDDF1", "Chile", "56"),
        new CountryCode("\uD83C\uDDE8\uD83C\uDDF3", "China", "86"),
        new CountryCode("\uD83C\uDDE8\uD83C\uDDF4", "Colombia", "57"),
        new CountryCode("\uD83C\uDDF0\uD83C\uDDF2", "Comoros", "269"),
        new CountryCode("\uD83C\uDDE8\uD83C\uDDE9", "Congo (DRC)", "243"),
        new CountryCode("\uD83C\uDDE8\uD83C\uDDEC", "Congo (Republic)", "242"),
        new CountryCode("\uD83C\uDDE8\uD83C\uDDF0", "Cook Islands", "682"),
        new CountryCode("\uD83C\uDDE8\uD83C\uDDF7", "Costa Rica", "506"),
        new CountryCode("\uD83C\uDDE8\uD83C\uDDEE", "Côte d'Ivoire", "225"),
        new CountryCode("\uD83C\uDDED\uD83C\uDDF7", "Croatia", "385"),
        new CountryCode("\uD83C\uDDE8\uD83C\uDDFA", "Cuba", "53"),
        new CountryCode("\uD83C\uDDE8\uD83C\uDDFE", "Cyprus", "357"),
        new CountryCode("\uD83C\uDDE8\uD83C\uDDFF", "Czech Republic", "420"),
        new CountryCode("\uD83C\uDDE9\uD83C\uDDF0", "Denmark", "45"),
        new CountryCode("\uD83C\uDDE9\uD83C\uDDEF", "Djibouti", "253"),
        new CountryCode("\uD83C\uDDE9\uD83C\uDDF2", "Dominica", "1767"),
        new CountryCode("\uD83C\uDDE9\uD83C\uDDF4", "Dominican Republic", "1809"),
        new CountryCode("\uD83C\uDDEA\uD83C\uDDE8", "Ecuador", "593"),
        new CountryCode("\uD83C\uDDEA\uD83C\uDDEC", "Egypt", "20"),
        new CountryCode("\uD83C\uDDF8\uD83C\uDDFB", "El Salvador", "503"),
        new CountryCode("\uD83C\uDDEC\uD83C\uDDF6", "Equatorial Guinea", "240"),
        new CountryCode("\uD83C\uDDEA\uD83C\uDDF7", "Eritrea", "291"),
        new CountryCode("\uD83C\uDDEA\uD83C\uDDEA", "Estonia", "372"),
        new CountryCode("\uD83C\uDDEA\uD83C\uDDF9", "Ethiopia", "251"),
        new CountryCode("\uD83C\uDDEB\uD83C\uDDF0", "Falkland Islands", "500"),
        new CountryCode("\uD83C\uDDEB\uD83C\uDDF4", "Faroe Islands", "298"),
        new CountryCode("\uD83C\uDDEB\uD83C\uDDEF", "Fiji", "679"),
        new CountryCode("\uD83C\uDDEB\uD83C\uDDEE", "Finland", "358"),
        new CountryCode("\uD83C\uDDEB\uD83C\uDDF7", "France", "33"),
        new CountryCode("\uD83C\uDDEC\uD83C\uDDEB", "French Guiana", "594"),
        new CountryCode("\uD83C\uDDF5\uD83C\uDDEB", "French Polynesia", "689"),
        new CountryCode("\uD83C\uDDEC\uD83C\uDDE6", "Gabon", "241"),
        new CountryCode("\uD83C\uDDEC\uD83C\uDDF2", "Gambia", "220"),
        new CountryCode("\uD83C\uDDEC\uD83C\uDDEA", "Georgia", "995"),
        new CountryCode("\uD83C\uDDE9\uD83C\uDDEA", "Germany", "49"),
        new CountryCode("\uD83C\uDDEC\uD83C\uDDED", "Ghana", "233"),
        new CountryCode("\uD83C\uDDEC\uD83C\uDDEE", "Gibraltar", "350"),
        new CountryCode("\uD83C\uDDEC\uD83C\uDDF7", "Greece", "30"),
        new CountryCode("\uD83C\uDDEC\uD83C\uDDF1", "Greenland", "299"),
        new CountryCode("\uD83C\uDDEC\uD83C\uDDE9", "Grenada", "1473"),
        new CountryCode("\uD83C\uDDEC\uD83C\uDDF5", "Guadeloupe", "590"),
        new CountryCode("\uD83C\uDDEC\uD83C\uDDFA", "Guam", "1671"),
        new CountryCode("\uD83C\uDDEC\uD83C\uDDF9", "Guatemala", "502"),
        new CountryCode("\uD83C\uDDEC\uD83C\uDDF3", "Guinea", "224"),
        new CountryCode("\uD83C\uDDEC\uD83C\uDDFC", "Guinea-Bissau", "245"),
        new CountryCode("\uD83C\uDDEC\uD83C\uDDFE", "Guyana", "592"),
        new CountryCode("\uD83C\uDDED\uD83C\uDDF9", "Haiti", "509"),
        new CountryCode("\uD83C\uDDED\uD83C\uDDF3", "Honduras", "504"),
        new CountryCode("\uD83C\uDDED\uD83C\uDDF0", "Hong Kong", "852"),
        new CountryCode("\uD83C\uDDED\uD83C\uDDFA", "Hungary", "36"),
        new CountryCode("\uD83C\uDDEE\uD83C\uDDF8", "Iceland", "354"),
        new CountryCode("\uD83C\uDDEE\uD83C\uDDF3", "India", "91"),
        new CountryCode("\uD83C\uDDEE\uD83C\uDDE9", "Indonesia", "62"),
        new CountryCode("\uD83C\uDDEE\uD83C\uDDF7", "Iran", "98"),
        new CountryCode("\uD83C\uDDEE\uD83C\uDDF6", "Iraq", "964"),
        new CountryCode("\uD83C\uDDEE\uD83C\uDDEA", "Ireland", "353"),
        new CountryCode("\uD83C\uDDEE\uD83C\uDDF1", "Israel", "972"),
        new CountryCode("\uD83C\uDDEE\uD83C\uDDF9", "Italy", "39"),
        new CountryCode("\uD83C\uDDEF\uD83C\uDDF2", "Jamaica", "1876"),
        new CountryCode("\uD83C\uDDEF\uD83C\uDDF5", "Japan", "81"),
        new CountryCode("\uD83C\uDDEF\uD83C\uDDF4", "Jordan", "962"),
        new CountryCode("\uD83C\uDDF0\uD83C\uDDFF", "Kazakhstan", "7"),
        new CountryCode("\uD83C\uDDF0\uD83C\uDDEA", "Kenya", "254"),
        new CountryCode("\uD83C\uDDF0\uD83C\uDDEE", "Kiribati", "686"),
        new CountryCode("\uD83C\uDDF0\uD83C\uDDF5", "North Korea", "850"),
        new CountryCode("\uD83C\uDDF0\uD83C\uDDF7", "South Korea", "82"),
        new CountryCode("\uD83C\uDDF0\uD83C\uDDFC", "Kuwait", "965"),
        new CountryCode("\uD83C\uDDF0\uD83C\uDDEC", "Kyrgyzstan", "996"),
        new CountryCode("\uD83C\uDDF1\uD83C\uDDE6", "Laos", "856"),
        new CountryCode("\uD83C\uDDF1\uD83C\uDDFB", "Latvia", "371"),
        new CountryCode("\uD83C\uDDF1\uD83C\uDDE7", "Lebanon", "961"),
        new CountryCode("\uD83C\uDDF1\uD83C\uDDF8", "Lesotho", "266"),
        new CountryCode("\uD83C\uDDF1\uD83C\uDDF7", "Liberia", "231"),
        new CountryCode("\uD83C\uDDF1\uD83C\uDDFE", "Libya", "218"),
        new CountryCode("\uD83C\uDDF1\uD83C\uDDEE", "Liechtenstein", "423"),
        new CountryCode("\uD83C\uDDF1\uD83C\uDDF9", "Lithuania", "370"),
        new CountryCode("\uD83C\uDDF1\uD83C\uDDFA", "Luxembourg", "352"),
        new CountryCode("\uD83C\uDDF2\uD83C\uDDF4", "Macau", "853"),
        new CountryCode("\uD83C\uDDF2\uD83C\uDDF0", "Macedonia", "389"),
        new CountryCode("\uD83C\uDDF2\uD83C\uDDEC", "Madagascar", "261"),
        new CountryCode("\uD83C\uDDF2\uD83C\uDDFC", "Malawi", "265"),
        new CountryCode("\uD83C\uDDF2\uD83C\uDDFE", "Malaysia", "60"),
        new CountryCode("\uD83C\uDDF2\uD83C\uDDFB", "Maldives", "960"),
        new CountryCode("\uD83C\uDDF2\uD83C\uDDF1", "Mali", "223"),
        new CountryCode("\uD83C\uDDF2\uD83C\uDDF9", "Malta", "356"),
        new CountryCode("\uD83C\uDDF2\uD83C\uDDED", "Marshall Islands", "692"),
        new CountryCode("\uD83C\uDDF2\uD83C\uDDF6", "Martinique", "596"),
        new CountryCode("\uD83C\uDDF2\uD83C\uDDF7", "Mauritania", "222"),
        new CountryCode("\uD83C\uDDF2\uD83C\uDDFA", "Mauritius", "230"),
        new CountryCode("\uD83C\uDDF2\uD83C\uDDFD", "Mexico", "52"),
        new CountryCode("\uD83C\uDDEB\uD83C\uDDF2", "Micronesia", "691"),
        new CountryCode("\uD83C\uDDF2\uD83C\uDDE9", "Moldova", "373"),
        new CountryCode("\uD83C\uDDF2\uD83C\uDDE8", "Monaco", "377"),
        new CountryCode("\uD83C\uDDF2\uD83C\uDDF3", "Mongolia", "976"),
        new CountryCode("\uD83C\uDDF2\uD83C\uDDEA", "Montenegro", "382"),
        new CountryCode("\uD83C\uDDF2\uD83C\uDDF8", "Montserrat", "1664"),
        new CountryCode("\uD83C\uDDF2\uD83C\uDDE6", "Morocco", "212"),
        new CountryCode("\uD83C\uDDF2\uD83C\uDDFF", "Mozambique", "258"),
        new CountryCode("\uD83C\uDDF2\uD83C\uDDF2", "Myanmar", "95"),
        new CountryCode("\uD83C\uDDF3\uD83C\uDDE6", "Namibia", "264"),
        new CountryCode("\uD83C\uDDF3\uD83C\uDDF7", "Nauru", "674"),
        new CountryCode("\uD83C\uDDF3\uD83C\uDDF5", "Nepal", "977"),
        new CountryCode("\uD83C\uDDF3\uD83C\uDDF1", "Netherlands", "31"),
        new CountryCode("\uD83C\uDDF3\uD83C\uDDE8", "New Caledonia", "687"),
        new CountryCode("\uD83C\uDDF3\uD83C\uDDFF", "New Zealand", "64"),
        new CountryCode("\uD83C\uDDF3\uD83C\uDDEE", "Nicaragua", "505"),
        new CountryCode("\uD83C\uDDF3\uD83C\uDDEA", "Niger", "227"),
        new CountryCode("\uD83C\uDDF3\uD83C\uDDEC", "Nigeria", "234"),
        new CountryCode("\uD83C\uDDF3\uD83C\uDDFA", "Niue", "683"),
        new CountryCode("\uD83C\uDDF3\uD83C\uDDF4", "Norway", "47"),
        new CountryCode("\uD83C\uDDF4\uD83C\uDDF2", "Oman", "968"),
        new CountryCode("\uD83C\uDDF5\uD83C\uDDF0", "Pakistan", "92"),
        new CountryCode("\uD83C\uDDF5\uD83C\uDDFC", "Palau", "680"),
        new CountryCode("\uD83C\uDDF5\uD83C\uDDF8", "Palestine", "970"),
        new CountryCode("\uD83C\uDDF5\uD83C\uDDE6", "Panama", "507"),
        new CountryCode("\uD83C\uDDF5\uD83C\uDDEC", "Papua New Guinea", "675"),
        new CountryCode("\uD83C\uDDF5\uD83C\uDDFE", "Paraguay", "595"),
        new CountryCode("\uD83C\uDDF5\uD83C\uDDEA", "Peru", "51"),
        new CountryCode("\uD83C\uDDF5\uD83C\uDDED", "Philippines", "63"),
        new CountryCode("\uD83C\uDDF5\uD83C\uDDF1", "Poland", "48"),
        new CountryCode("\uD83C\uDDF5\uD83C\uDDF9", "Portugal", "351"),
        new CountryCode("\uD83C\uDDF5\uD83C\uDDF7", "Puerto Rico", "1787"),
        new CountryCode("\uD83C\uDDF6\uD83C\uDDE6", "Qatar", "974"),
        new CountryCode("\uD83C\uDDF7\uD83C\uDDEA", "Réunion", "262"),
        new CountryCode("\uD83C\uDDF7\uD83C\uDDF4", "Romania", "40"),
        new CountryCode("\uD83C\uDDF7\uD83C\uDDFA", "Russia", "7"),
        new CountryCode("\uD83C\uDDF7\uD83C\uDDFC", "Rwanda", "250"),
        new CountryCode("\uD83C\uDDF0\uD83C\uDDF3", "Saint Kitts and Nevis", "1869"),
        new CountryCode("\uD83C\uDDF1\uD83C\uDDE8", "Saint Lucia", "1758"),
        new CountryCode("\uD83C\uDDFB\uD83C\uDDE8", "Saint Vincent and the Grenadines", "1784"),
        new CountryCode("\uD83C\uDDFC\uD83C\uDDF8", "Samoa", "685"),
        new CountryCode("\uD83C\uDDF8\uD83C\uDDF2", "San Marino", "378"),
        new CountryCode("\uD83C\uDDF8\uD83C\uDDF9", "São Tomé and Príncipe", "239"),
        new CountryCode("\uD83C\uDDF8\uD83C\uDDE6", "Saudi Arabia", "966"),
        new CountryCode("\uD83C\uDDF8\uD83C\uDDF3", "Senegal", "221"),
        new CountryCode("\uD83C\uDDF7\uD83C\uDDF8", "Serbia", "381"),
        new CountryCode("\uD83C\uDDF8\uD83C\uDDE8", "Seychelles", "248"),
        new CountryCode("\uD83C\uDDF8\uD83C\uDDF1", "Sierra Leone", "232"),
        new CountryCode("\uD83C\uDDF8\uD83C\uDDEC", "Singapore", "65"),
        new CountryCode("\uD83C\uDDF8\uD83C\uDDF0", "Slovakia", "421"),
        new CountryCode("\uD83C\uDDF8\uD83C\uDDEE", "Slovenia", "386"),
        new CountryCode("\uD83C\uDDF8\uD83C\uDDE7", "Solomon Islands", "677"),
        new CountryCode("\uD83C\uDDF8\uD83C\uDDF4", "Somalia", "252"),
        new CountryCode("\uD83C\uDDFF\uD83C\uDDE6", "South Africa", "27"),
        new CountryCode("\uD83C\uDDF8\uD83C\uDDF8", "South Sudan", "211"),
        new CountryCode("\uD83C\uDDEA\uD83C\uDDF8", "Spain", "34"),
        new CountryCode("\uD83C\uDDF1\uD83C\uDDF0", "Sri Lanka", "94"),
        new CountryCode("\uD83C\uDDF8\uD83C\uDDE9", "Sudan", "249"),
        new CountryCode("\uD83C\uDDF8\uD83C\uDDF7", "Suriname", "597"),
        new CountryCode("\uD83C\uDDF8\uD83C\uDDFF", "Swaziland", "268"),
        new CountryCode("\uD83C\uDDF8\uD83C\uDDEA", "Sweden", "46"),
        new CountryCode("\uD83C\uDDE8\uD83C\uDDED", "Switzerland", "41"),
        new CountryCode("\uD83C\uDDF8\uD83C\uDDFE", "Syria", "963"),
        new CountryCode("\uD83C\uDDF9\uD83C\uDDFC", "Taiwan", "886"),
        new CountryCode("\uD83C\uDDF9\uD83C\uDDEF", "Tajikistan", "992"),
        new CountryCode("\uD83C\uDDF9\uD83C\uDDFF", "Tanzania", "255"),
        new CountryCode("\uD83C\uDDF9\uD83C\uDDED", "Thailand", "66"),
        new CountryCode("\uD83C\uDDF9\uD83C\uDDF1", "Timor-Leste", "670"),
        new CountryCode("\uD83C\uDDF9\uD83C\uDDEC", "Togo", "228"),
        new CountryCode("\uD83C\uDDF9\uD83C\uDDF0", "Tokelau", "690"),
        new CountryCode("\uD83C\uDDF9\uD83C\uDDF4", "Tonga", "676"),
        new CountryCode("\uD83C\uDDF9\uD83C\uDDF9", "Trinidad and Tobago", "1868"),
        new CountryCode("\uD83C\uDDF9\uD83C\uDDF3", "Tunisia", "216"),
        new CountryCode("\uD83C\uDDF9\uD83C\uDDF7", "Turkey", "90"),
        new CountryCode("\uD83C\uDDF9\uD83C\uDDF2", "Turkmenistan", "993"),
        new CountryCode("\uD83C\uDDF9\uD83C\uDDE8", "Turks and Caicos Islands", "1649"),
        new CountryCode("\uD83C\uDDF9\uD83C\uDDFB", "Tuvalu", "688"),
        new CountryCode("\uD83C\uDDFA\uD83C\uDDEC", "Uganda", "256"),
        new CountryCode("\uD83C\uDDFA\uD83C\uDDE6", "Ukraine", "380"),
        new CountryCode("\uD83C\uDDE6\uD83C\uDDEA", "United Arab Emirates", "971"),
        new CountryCode("\uD83C\uDDEC\uD83C\uDDE7", "United Kingdom", "44"),
        new CountryCode("\uD83C\uDDFA\uD83C\uDDF8", "United States", "1"),
        new CountryCode("\uD83C\uDDFA\uD83C\uDDFE", "Uruguay", "598"),
        new CountryCode("\uD83C\uDDFA\uD83C\uDDFF", "Uzbekistan", "998"),
        new CountryCode("\uD83C\uDDFB\uD83C\uDDFA", "Vanuatu", "678"),
        new CountryCode("\uD83C\uDDFB\uD83C\uDDE6", "Vatican City", "379"),
        new CountryCode("\uD83C\uDDFB\uD83C\uDDEA", "Venezuela", "58"),
        new CountryCode("\uD83C\uDDFB\uD83C\uDDF3", "Vietnam", "84"),
        new CountryCode("\uD83C\uDDFB\uD83C\uDDEC", "British Virgin Islands", "1284"),
        new CountryCode("\uD83C\uDDFB\uD83C\uDDEE", "U.S. Virgin Islands", "1340"),
        new CountryCode("\uD83C\uDDFC\uD83C\uDDEB", "Wallis and Futuna", "681"),
        new CountryCode("\uD83C\uDDFE\uD83C\uDDEA", "Yemen", "967"),
        new CountryCode("\uD83C\uDDFF\uD83C\uDDF2", "Zambia", "260"),
        new CountryCode("\uD83C\uDDFF\uD83C\uDDFC", "Zimbabwe", "263"),
    };
}
