package com.fzb.blog.web.plugin;

import com.fzb.blog.common.Constants;
import com.fzb.blog.util.BlogBuildInfoUtil;
import com.fzb.common.util.http.HttpUtil;
import flexjson.JSONDeserializer;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;

class UpdateVersionTimerTask extends TimerTask {

    private static final Logger LOGGER = Logger.getLogger(UpdateVersionTimerTask.class);

    private boolean checkPreview;
    private Version version;

    public UpdateVersionTimerTask(boolean checkPreview) {
        this.checkPreview = checkPreview;
    }

    @Override
    public void run() {
        try {
            String versionUrl;
            if (checkPreview) {
                versionUrl = Constants.ZRLOG_RESOURCE_DOWNLOAD_URL + "/preview/last.version.json";
            } else {
                versionUrl = Constants.ZRLOG_RESOURCE_DOWNLOAD_URL + "/release/last.version.json";
            }
            String txtContent = HttpUtil.getTextByUrl(versionUrl + "?_" + System.currentTimeMillis()).trim();
            Version tLastVersion = new JSONDeserializer<Version>().deserialize(txtContent, Version.class);
            LOGGER.info(txtContent);
            tLastVersion.setChangeLog(HttpUtil.getTextByUrl("http://www.zrlog.com/changelog/" + tLastVersion.getVersion() + "-" + tLastVersion.getBuildId() + ".html"));
            Date buildDate = new SimpleDateFormat("yyyy-MM-dd hh:mm").parse(tLastVersion.getReleaseDate());
            if (!tLastVersion.getBuildId().equals(BlogBuildInfoUtil.getBuildId()) && buildDate.after(BlogBuildInfoUtil.getTime())) {
                LOGGER.info("ZrLog New update found new [" + tLastVersion.getVersion() + "-" + tLastVersion.getBuildId() + "]");
                if (BlogBuildInfoUtil.isDev()) {
                    LOGGER.info("Maybe need clone again from git repo");
                }
                this.version = tLastVersion;
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    public Version getVersion() {
        return version;
    }
}