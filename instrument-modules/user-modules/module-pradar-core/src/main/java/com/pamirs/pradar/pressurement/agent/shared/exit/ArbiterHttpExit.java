/**
 * Copyright 2021 Shulie Technology, Co.Ltd
 * Email: shulie@shulie.io
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pamirs.pradar.pressurement.agent.shared.exit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.pamirs.pradar.PradarSwitcher;
import com.pamirs.pradar.pressurement.agent.shared.custominterfacebase.Exit;
import com.pamirs.pradar.pressurement.agent.shared.service.GlobalConfig;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * ArbiterHttpExit
 *
 * @author 311183
 */
public class ArbiterHttpExit implements Exit {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArbiterHttpExit.class);

    private static LoadingCache<String, Boolean> patternCache = CacheBuilder.newBuilder()
            .maximumSize(300).expireAfterAccess(5 * 60, TimeUnit.SECONDS).build(
                    new CacheLoader<String, Boolean>() {
                        @Override
                        public Boolean load(String name) throws Exception {
                            Set<String> urlWhiteList = GlobalConfig.getInstance().getUrlWhiteList();
                            if (urlWhiteList != null) {
                                for (String urlWhite : urlWhiteList) {
                                    if (matching(name, urlWhite)) {
                                        return true;
                                    }
                                }
                            }
                            return false;
                        }
                    }
            );

    private static LoadingCache<String, Boolean> httpMatchResult = CacheBuilder.newBuilder()
            .maximumSize(300).expireAfterAccess(5 * 60, TimeUnit.SECONDS).build(
                    new CacheLoader<String, Boolean>() {
                        @Override
                        public Boolean load(String url) throws Exception {
                            return shallWePassHttpStringCache(url);
                        }
                    }
            );

    private static LoadingCache<String, Boolean> rpcMatchResult = CacheBuilder.newBuilder()
            .maximumSize(100).expireAfterAccess(5 * 60, TimeUnit.SECONDS).build(
                    new CacheLoader<String, Boolean>() {
                        @Override
                        public Boolean load(String name) throws Exception {
                            return getRpcPassedCache(name);
                        }
                    }
            );

    public static void clearRpcMatch() {
        rpcMatchResult.invalidateAll();
    }

    public static void clearHttpMatch() {
        httpMatchResult.invalidateAll();
        patternCache.invalidateAll();
    }

    /**
     * ??????????????????????????? rpc ??????
     * ?????????????????????
     *
     * @param className ??????
     */
    public static boolean shallWePassRpc(String className) {
        if (!PradarSwitcher.whiteListSwitchOn()) {
            return true;
        }
        return rpcMatchResult.getUnchecked(className);
    }

    /**
     * ??????????????????????????? rpc ??????
     * ?????? ??????+????????? ???????????????
     *
     * @param className  ??????
     * @param methodName ?????????
     */
    public static boolean shallWePassRpc(String className, String methodName) {
        if (!PradarSwitcher.whiteListSwitchOn()) {
            return true;
        }
        if (StringUtils.isBlank(methodName)) {
            return rpcMatchResult.getUnchecked(className);
        }
        return rpcMatchResult.getUnchecked(className + '#' + methodName);
    }

    private static Boolean getRpcPassedCache(String name) {
        if (StringUtils.isBlank(name)) {
            return Boolean.FALSE;
        }

        Set<String> rpcNameList = GlobalConfig.getInstance().getRpcNameWhiteList();
        if (rpcNameList == null) {
            return Boolean.FALSE;
        }
        if (rpcNameList.contains(name)) {
            return Boolean.TRUE;
        }
        if (StringUtils.indexOf(name, '#') != -1) {
            return Boolean.FALSE;
        } else {
            for (String value : rpcNameList) {
                if (StringUtils.indexOf(value, '#') == -1) {
                    if (StringUtils.equals(value, name)) {
                        return Boolean.TRUE;
                    }
                } else {
                    // ????????????????????? # ??????????????????????????????
                    String className = StringUtils.substring(value, StringUtils.indexOf(value, '#'));
                    if (StringUtils.equals(value, className)) {
                        return Boolean.TRUE;
                    }
                }
            }
        }
        return Boolean.FALSE;
    }

    /**
     * ??????????????????????????????????????????
     * XingNG???rpc url??????
     */
    public static boolean shallWePassHttpStringXingNg(String url) {
        if (!PradarSwitcher.whiteListSwitchOn()) {
            // ???????????? or ?????????????????????
            return true;
        }
        //?????????????????????????????????????????????
        boolean arbiterAllowRequest = false;
        if (url != null) {
            if (patternCache.getUnchecked(url)) {
                return true;
            }
        }
        return arbiterAllowRequest;
    }

    /**
     * ??????????????????????????????????????????
     */
    public static boolean shallWePassHttpString(String url) {
        try {
            if (!PradarSwitcher.whiteListSwitchOn()) {
                return true;
            }
            Boolean result = httpMatchResult.get(url);
            return result;
        } catch (ExecutionException e) {
            LOGGER.warn("WhiteListError: shallWePassHttpString cache exception!", e);
            return false;
        }
    }

    private static boolean shallWePassHttpStringCache(String url) {
        if (url != null && url.indexOf("&#47;") != -1) {
            url = url.replace("&#47;", "/");
        }
        String orgUrl = url;
        //?????????????????????????????????????????????
        boolean arbiterAllowRequest = false;
        if (url != null) {
            try {
                URI uri = URI.create(url);
                if (uri.getHost().startsWith("pt")) {
                    try {
                        url = new URI(uri.getScheme(),
                                uri.getUserInfo(),
                                uri.getHost().substring(2),
                                uri.getPort(),
                                uri.getPath(),
                                uri.getQuery(),
                                uri.getFragment())
                                .getPath();
                    } catch (URISyntaxException e) {
                    }
                } else {
                    url = uri.getPath();
                }
            } catch (Throwable e) {
                if (url.startsWith("http://")) {
                    url = url.substring(7);
                    if (url.indexOf("/") != -1) {
                        url = url.substring(url.indexOf('/') + 1);
                    } else {
                        url = "/";
                    }
                } else if (url.startsWith("https://")) {
                    url = url.substring(8);
                    if (url.indexOf("/") != -1) {
                        url = url.substring(url.indexOf('/') + 1);
                    } else {
                        url = "/";
                    }
                }
                if (url.indexOf('?') != -1) {
                    url = url.substring(0, url.indexOf('?'));
                }
                if (url.indexOf('#') != -1) {
                    url = url.substring(0, url.indexOf('#'));
                }
                //???????????????????????????uri????????????????????????
            }

            if (StringUtils.isBlank(url) || "/".equals(url)) {
                /**
                 * ?????? url ???????????????/?????????????????????????????? url ????????????
                 */
                if (patternCache.getUnchecked(orgUrl)) {
                    return true;
                }
            }
            if (patternCache.getUnchecked(url)) {
                return true;
            }
            if (null != PradarSwitcher.httpPassPrefix.get()
                    && url.startsWith(PradarSwitcher.httpPassPrefix.get())) {
                return true;
            }
            LOGGER.warn("WhiteListError: url is not allowed:" + url);
        }
        return arbiterAllowRequest;
    }

    /**
     * ????????????????????????
     * <p>
     * ????????????????????????????????????????????????(*)?????????(?)????????????Simulator????????????????????????????????????????????????
     * ?????????java.lang.String????????????"*String"?????????
     * </p>
     * <ul>
     * <li>(null) matching (null) == false</li>
     * <li>    ANY matching ("*") == true</li>
     * </ul>
     *
     * @param string   ???????????????
     * @param wildcard ?????????????????????
     * @return true:?????????????????????????????????;false:????????????????????????????????????
     */
    public static boolean matching(final String string, final String wildcard) {
        if ("*".equals(wildcard)) {
            return true;
        }
        if (wildcard == null || string == null) {
            return false;
        }
        /**
         * ?????????????????????????????????
         */
        if (wildcard.indexOf("*") == -1) {
            return wildcard.equals(string);
        }
        return null != wildcard
                && null != string
                && matching(string, wildcard, 0, 0);
    }

    /**
     * Internal matching recursive function.
     */
    private static boolean matching(String string, String wildcard, int stringStartNdx, int patternStartNdx) {
        int pNdx = patternStartNdx;
        int sNdx = stringStartNdx;
        int pLen = wildcard.length();
        if (pLen == 1) {
            if (wildcard.charAt(0) == '*') {     // speed-up
                return true;
            }
        }
        int sLen = string.length();
        boolean nextIsNotWildcard = false;

        while (true) {

            // check if end of string and/or pattern occurred
            if ((sNdx >= sLen)) {   // end of string still may have pending '*' callback pattern
                while ((pNdx < pLen) && (wildcard.charAt(pNdx) == '*')) {
                    pNdx++;
                }
                return pNdx >= pLen;
            }
            if (pNdx >= pLen) {         // end of pattern, but not end of the string
                return false;
            }
            char p = wildcard.charAt(pNdx);    // pattern char

            // perform logic
            if (!nextIsNotWildcard) {

                if (p == '\\') {
                    pNdx++;
                    nextIsNotWildcard = true;
                    continue;
                }
                if (p == '?') {
                    sNdx++;
                    pNdx++;
                    continue;
                }
                if (p == '*') {
                    char pnext = 0;           // next pattern char
                    if (pNdx + 1 < pLen) {
                        pnext = wildcard.charAt(pNdx + 1);
                    }
                    if (pnext == '*') {         // double '*' have the same effect as one '*'
                        pNdx++;
                        continue;
                    }
                    int i;
                    pNdx++;

                    // find recursively if there is any substring from the end of the
                    // line that matches the rest of the pattern !!!
                    for (i = string.length(); i >= sNdx; i--) {
                        if (matching(string, wildcard, i, pNdx)) {
                            return true;
                        }
                    }
                    return false;
                }
            } else {
                nextIsNotWildcard = false;
            }

            // check if pattern char and string char are equals
            if (p != string.charAt(sNdx)) {
                return false;
            }

            // everything matches for now, continue
            sNdx++;
            pNdx++;
        }
    }

}
