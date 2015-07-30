package com.jidian.indexing;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * This converter converts the app into json format ready for elasticsearch build.
 *
 * Created by xiaoyun on 4/26/14.
 */
public class RawToIndexingConverter implements Function<RawApp, String> {
    public final static String localeKey = "zh_CN";
    boolean useReview = true;

    public RawToIndexingConverter() {}

    public RawToIndexingConverter(boolean useReviewFlag) {
        useReview = useReviewFlag;
    }

    /**
     * @return the predicate that can tell whether the rawApp has basic info for build.
     */
    public static Predicate<RawApp> getGoodAppTester() {
        return rawApp -> {
            if (rawApp.android_id == null) return false;
            if (rawApp == null) return false;
            if (!rawApp.locales.containsKey(localeKey)) return false;
            RawApp.Locale locale = rawApp.locales.get(localeKey);
            if (locale.icon_url == null) return false;
            if (locale.name == null) return false;
            if (rawApp.url == null) return false;
            return true;
        };
    }

    @Override
    public String apply(RawApp rawApp) {
        if (!rawApp.locales.containsKey(localeKey)) return null;
        RawApp.Locale locale = rawApp.locales.get(localeKey);

        XContentBuilder source = null;
        try {
            source = XContentFactory.jsonBuilder().startObject();

            // We need to set up the source to reflect what is in rawApp.
            source.field(EsField.ID.getKeyStr(), rawApp.android_id);
            source.field(EsField.TITLE.getKeyStr(), locale.name);


            if (rawApp.author != null && rawApp.author.name != null) {
                source.field(EsField.KEYWORDS.getKeyStr(), rawApp.author.name);
            }

            if (rawApp.whatsnew != null) {
                source.field(EsField.BODY.getKeyStr(), rawApp.whatsnew);
            }

            if (locale.description != null) {
                source.field(EsField.BODY.getKeyStr(), locale.description);
            }

            if (useReview && rawApp.reviews != null) {
                List<RawApp.Review> reviews = rawApp.reviews;
                for (RawApp.Review review : reviews) {
                    if (review.text != null) {
                        source.field(EsField.BODY.getKeyStr(), review.text);
                    }
                }
            }

            if (locale.icon_url != null) {
                source.field(EsField.ICON_URL.getKeyStr(), locale.icon_url);
            }

            if (rawApp.url != null) {
                source.field(EsField.APP_URL.getKeyStr(), rawApp.url);
            }

            source.field(EsField.NUM_OF_RATINGS.getKeyStr(), Math.log(1+rawApp.rating_count));
            source.field(EsField.NUM_OF_DOWNLOADS.getKeyStr(), Math.log(1 + rawApp.downloads));

            if (rawApp.version != null) {
                source.field(EsField.VERSION.getKeyStr(), rawApp.version);
            }

            source.endObject();
            return source.string();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
