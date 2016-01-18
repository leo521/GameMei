package gamemei.qiyun.com.gamemei.bean;

import java.util.List;

/**
 * 游戏列表的Bean
 *
 * @author hfcui
 */
public class Top_InformationBean {

    public List<Information> informations;

    public class Information {

        // 资讯标题
        public String information_title;
        // 资讯内容
        public String information_content;
        // 资讯点击量
        public String information_hits;
        // 资讯发布时间
        public String information_time;
        // 资讯图片URL地址
        public String information_image_url;


        public String getInformation_image_url() {
            return information_image_url;
        }

        public void setInformation_image_url(String information_image_url) {
            this.information_image_url = information_image_url;
        }

        public String getInformation_time() {
            return information_time;
        }

        public void setInformation_time(String information_time) {
            this.information_time = information_time;
        }

        public String getInformation_title() {
            return information_title;
        }

        public void setInformation_title(String information_title) {
            this.information_title = information_title;
        }

        public String getInformation_content() {
            return information_content;
        }

        public void setInformation_content(String information_content) {
            this.information_content = information_content;
        }

        public String getInformation_hits() {
            return information_hits;
        }

        public void setInformation_hits(String information_hits) {
            this.information_hits = information_hits;
        }

        @Override
        public String toString() {
            return "Information{" +
                    "information_title='" + information_title + '\'' +
                    ", information_content='" + information_content + '\'' +
                    ", information_hits='" + information_hits + '\'' +
                    ", information_time='" + information_time + '\'' +
                    ", information_image_url='" + information_image_url + '\'' +
                    '}';
        }
    }
}
