package xyz.gianlu.librespot.mercury;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.protobuf.AbstractMessage;
import com.google.protobuf.ProtocolStringList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.gianlu.librespot.common.Utils;
import xyz.gianlu.librespot.common.proto.Mercury;
import xyz.gianlu.librespot.common.proto.Metadata;
import xyz.gianlu.librespot.common.proto.Playlist4Changes;
import xyz.gianlu.librespot.common.proto.Playlist4Content;
import xyz.gianlu.librespot.mercury.model.AlbumId;
import xyz.gianlu.librespot.mercury.model.ArtistId;
import xyz.gianlu.librespot.mercury.model.PlaylistId;
import xyz.gianlu.librespot.mercury.model.TrackId;

import java.util.List;

/**
 * @author Gianlu
 */
public final class MercuryRequests {
    private static final ProtoJsonMercuryRequest.JsonConverter<Playlist4Changes.SelectedListContent> SELECTED_LIST_CONTENT_JSON_CONVERTER = proto -> {
        List<Playlist4Content.Item> items = proto.getContents().getItemsList();
        JsonArray array = new JsonArray(items.size());
        for (Playlist4Content.Item item : items) array.add(item.getUri());
        return array;
    };
    private static final ProtoJsonMercuryRequest.JsonConverter<Metadata.Date> DATE_JSON_CONVERTER = proto -> {
        JsonObject obj = new JsonObject();
        obj.addProperty("year", proto.getYear());
        obj.addProperty("month", proto.getMonth());
        obj.addProperty("day", proto.getDay());
        return obj;
    };
    private static final ProtoJsonMercuryRequest.JsonConverter<Metadata.Restriction> RESTRICTION_JSON_CONVERTER = proto -> {
        JsonObject obj = new JsonObject();
        obj.addProperty("allowed", proto.getCountriesAllowed());
        obj.addProperty("forbidden", proto.getCountriesForbidden());
        obj.addProperty("type", proto.getTyp().name());
        putArray(obj, "catalogues", proto.getCatalogueStrList());
        return obj;
    };
    private static final ProtoJsonMercuryRequest.JsonConverter<Metadata.Copyright> COPYRIGHT_JSON_CONVERTER = proto -> {
        JsonObject obj = new JsonObject();
        obj.addProperty("text", proto.getText());
        obj.addProperty("type", proto.getTyp().name());
        return obj;
    };
    private static final ProtoJsonMercuryRequest.JsonConverter<Metadata.Image> IMAGE_JSON_CONVERTER = proto -> {
        JsonObject obj = new JsonObject();
        obj.addProperty("width", proto.getWidth());
        obj.addProperty("height", proto.getHeight());
        obj.addProperty("size", proto.getSize().name());
        obj.addProperty("fileId", Utils.toBase64(proto.getFileId()));
        return obj;
    };
    private static final ProtoJsonMercuryRequest.JsonConverter<Metadata.ExternalId> EXTERNAL_ID_JSON_CONVERTER = proto -> {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", proto.getTyp());
        obj.addProperty("id", proto.getId());
        return obj;
    };
    private static final ProtoJsonMercuryRequest.JsonConverter<Metadata.ActivityPeriod> ACTIVITY_PERIOD_JSON_CONVERTER = proto -> {
        JsonObject obj = new JsonObject();
        obj.addProperty("startYear", proto.getStartYear());
        obj.addProperty("endYear", proto.getEndYear());
        obj.addProperty("decade", proto.getDecade());
        return obj;
    };
    private static final ProtoJsonMercuryRequest.JsonConverter<Metadata.AudioFile> AUDIO_FILE_JSON_CONVERTER = proto -> {
        JsonObject obj = new JsonObject();
        obj.addProperty("fileId", Utils.toBase64(proto.getFileId()));
        obj.addProperty("format", proto.getFormat().name());
        return obj;
    };
    private static final ProtoJsonMercuryRequest.JsonConverter<Metadata.Artist> ARTIST_JSON_CONVERTER;
    private static final ProtoJsonMercuryRequest.JsonConverter<Metadata.Album> ALBUM_JSON_CONVERTER;
    private static final ProtoJsonMercuryRequest.JsonConverter<Metadata.AlbumGroup> ALBUM_GROUP_JSON_CONVERTER;
    private static final ProtoJsonMercuryRequest.JsonConverter<Metadata.Track> TRACK_JSON_CONVERTER;
    private static final ProtoJsonMercuryRequest.JsonConverter<Metadata.Disc> DISC_JSON_CONVERTER;
    private static final ProtoJsonMercuryRequest.JsonConverter<Metadata.TopTracks> TOP_TRACKS_JSON_CONVERTER;
    private static final ProtoJsonMercuryRequest.JsonConverter<Metadata.SalePeriod> SALE_PERIOD_JSON_CONVERTER;
    private static final ProtoJsonMercuryRequest.JsonConverter<Metadata.ImageGroup> IMAGE_GROUP_JSON_CONVERTER;
    private static final ProtoJsonMercuryRequest.JsonConverter<Metadata.Biography> BIOGRAPHY_JSON_CONVERTER;

    static {
        SALE_PERIOD_JSON_CONVERTER = proto -> {
            JsonObject obj = new JsonObject();
            obj.add("start", DATE_JSON_CONVERTER.convert(proto.getStart()));
            obj.add("end", DATE_JSON_CONVERTER.convert(proto.getEnd()));
            putArray(obj, "restrictions", proto.getRestrictionList(), RESTRICTION_JSON_CONVERTER);
            return obj;
        };
        IMAGE_GROUP_JSON_CONVERTER = proto -> {
            JsonObject obj = new JsonObject();
            putArray(obj, "images", proto.getImageList(), IMAGE_JSON_CONVERTER);
            return obj;
        };
        BIOGRAPHY_JSON_CONVERTER = proto -> {
            JsonObject obj = new JsonObject();
            obj.addProperty("text", proto.getText());
            putArray(obj, "portraits", proto.getPortraitList(), IMAGE_JSON_CONVERTER);
            putArray(obj, "portraitGroups", proto.getPortraitGroupList(), IMAGE_GROUP_JSON_CONVERTER);
            return obj;
        };
        ARTIST_JSON_CONVERTER = new ProtoJsonMercuryRequest.JsonConverter<Metadata.Artist>() {
            @Override
            public @NotNull JsonElement convert(Metadata.@NotNull Artist proto) {
                JsonObject obj = new JsonObject();
                obj.addProperty("gid", Utils.bytesToHex(proto.getGid()));
                obj.addProperty("name", proto.getName());
                obj.addProperty("popularity", proto.getPopularity());
                obj.addProperty("isPortraitAlbumCover", proto.getIsPortraitAlbumCover());
                obj.add("portraitGroup", IMAGE_GROUP_JSON_CONVERTER.convert(proto.getPortraitGroup()));
                putArray(obj, "genres", proto.getGenreList());
                putArray(obj, "restrictions", proto.getRestrictionList(), RESTRICTION_JSON_CONVERTER);
                putArray(obj, "externalIds", proto.getExternalIdList(), EXTERNAL_ID_JSON_CONVERTER);
                putArray(obj, "related", proto.getRelatedList(), this);
                putArray(obj, "portraits", proto.getPortraitList(), IMAGE_JSON_CONVERTER);
                putArray(obj, "albumGroups", proto.getAlbumGroupList(), ALBUM_GROUP_JSON_CONVERTER);
                putArray(obj, "singleGroups", proto.getSingleGroupList(), ALBUM_GROUP_JSON_CONVERTER);
                putArray(obj, "compilationGroups", proto.getCompilationGroupList(), ALBUM_GROUP_JSON_CONVERTER);
                putArray(obj, "appearsOnGroups", proto.getAppearsOnGroupList(), ALBUM_GROUP_JSON_CONVERTER);
                putArray(obj, "biographies", proto.getBiographyList(), BIOGRAPHY_JSON_CONVERTER);
                putArray(obj, "topTracks", proto.getTopTrackList(), TOP_TRACKS_JSON_CONVERTER);
                putArray(obj, "activityPeriods", proto.getActivityPeriodList(), ACTIVITY_PERIOD_JSON_CONVERTER);
                return obj;
            }
        };
        ALBUM_JSON_CONVERTER = new ProtoJsonMercuryRequest.JsonConverter<Metadata.Album>() {
            @Override
            public @NotNull JsonElement convert(Metadata.@NotNull Album proto) {
                JsonObject obj = new JsonObject();
                obj.addProperty("gid", Utils.bytesToHex(proto.getGid()));
                obj.addProperty("name", proto.getName());
                obj.addProperty("popularity", proto.getPopularity());
                obj.addProperty("label", proto.getLabel());
                putArray(obj, "genres", proto.getGenreList());
                putArray(obj, "reviews", proto.getReviewList());
                putArray(obj, "artists", proto.getArtistList(), ARTIST_JSON_CONVERTER);
                putArray(obj, "related", proto.getRelatedList(), ALBUM_JSON_CONVERTER);
                obj.addProperty("type", proto.getTyp().name());
                obj.add("date", DATE_JSON_CONVERTER.convert(proto.getDate()));
                putArray(obj, "discs", proto.getDiscList(), DISC_JSON_CONVERTER);
                putArray(obj, "salePeriods", proto.getSalePeriodList(), SALE_PERIOD_JSON_CONVERTER);
                putArray(obj, "restrictions", proto.getRestrictionList(), RESTRICTION_JSON_CONVERTER);
                putArray(obj, "copyrights", proto.getCopyrightList(), COPYRIGHT_JSON_CONVERTER);
                obj.add("coverGroup", IMAGE_GROUP_JSON_CONVERTER.convert(proto.getCoverGroup()));
                putArray(obj, "covers", proto.getCoverList(), IMAGE_JSON_CONVERTER);
                putArray(obj, "externalIds", proto.getExternalIdList(), EXTERNAL_ID_JSON_CONVERTER);
                return obj;
            }
        };
        ALBUM_GROUP_JSON_CONVERTER = proto -> {
            JsonObject obj = new JsonObject();
            putArray(obj, "albums", proto.getAlbumList(), ALBUM_JSON_CONVERTER);
            return obj;
        };
        TRACK_JSON_CONVERTER = new ProtoJsonMercuryRequest.JsonConverter<Metadata.Track>() {
            @Override
            public @NotNull JsonElement convert(Metadata.@NotNull Track proto) {
                JsonObject obj = new JsonObject();
                obj.addProperty("gid", Utils.bytesToHex(proto.getGid()));
                obj.addProperty("name", proto.getName());
                obj.addProperty("number", proto.getNumber());
                obj.addProperty("discNumber", proto.getDiscNumber());
                obj.addProperty("duration", proto.getDuration());
                obj.addProperty("popularity", proto.getPopularity());
                obj.addProperty("explicit", proto.getExplicit());
                obj.add("album", ALBUM_JSON_CONVERTER.convert(proto.getAlbum()));
                putArray(obj, "artists", proto.getArtistList(), ARTIST_JSON_CONVERTER);
                putArray(obj, "externalIds", proto.getExternalIdList(), EXTERNAL_ID_JSON_CONVERTER);
                putArray(obj, "restrictions", proto.getRestrictionList(), RESTRICTION_JSON_CONVERTER);
                putArray(obj, "alternatives", proto.getAlternativeList(), this);
                putArray(obj, "salePeriods", proto.getSalePeriodList(), SALE_PERIOD_JSON_CONVERTER);
                putArray(obj, "previews", proto.getPreviewList(), AUDIO_FILE_JSON_CONVERTER);
                putArray(obj, "files", proto.getFileList(), AUDIO_FILE_JSON_CONVERTER);
                return obj;
            }
        };
        DISC_JSON_CONVERTER = proto -> {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", proto.getName());
            obj.addProperty("number", proto.getNumber());
            putArray(obj, "tracks", proto.getTrackList(), TRACK_JSON_CONVERTER);
            return obj;
        };
        TOP_TRACKS_JSON_CONVERTER = proto -> {
            JsonObject obj = new JsonObject();
            obj.addProperty("country", proto.getCountry());
            putArray(obj, "tracks", proto.getTrackList(), TRACK_JSON_CONVERTER);
            return obj;
        };
    }

    private MercuryRequests() {
    }

    private static <P extends AbstractMessage> void putArray(@NotNull JsonObject dest, @NotNull String key, @NotNull List<P> list, @NotNull ProtoJsonMercuryRequest.JsonConverter<P> converter) {
        if (!list.isEmpty()) dest.add(key, makeArray(list, converter));
    }

    private static void putArray(@NotNull JsonObject dest, @NotNull String key, @NotNull ProtocolStringList list) {
        if (!list.isEmpty()) dest.add(key, makeArray(list));
    }

    @NotNull
    private static JsonArray makeArray(@NotNull ProtocolStringList list) {
        JsonArray array = new JsonArray(list.size());
        for (String item : list) array.add(item);
        return array;
    }

    @NotNull
    private static <P extends AbstractMessage> JsonArray makeArray(@NotNull List<P> list, @NotNull ProtoJsonMercuryRequest.JsonConverter<P> converter) {
        JsonArray array = new JsonArray(list.size());
        for (P proto : list) array.add(converter.convert(proto));
        return array;
    }

    @NotNull
    public static ProtoJsonMercuryRequest<Playlist4Changes.SelectedListContent> getRootPlaylists(@NotNull String username) {
        return new ProtoJsonMercuryRequest<>(RawMercuryRequest.get(String.format("hm://playlist/user/%s/rootlist", username)),
                Playlist4Changes.SelectedListContent.parser(), SELECTED_LIST_CONTENT_JSON_CONVERTER);
    }

    @NotNull
    public static ProtoJsonMercuryRequest<Playlist4Changes.SelectedListContent> getPlaylist(@NotNull PlaylistId id) {
        return new ProtoJsonMercuryRequest<>(RawMercuryRequest.get(id.toMercuryUri()),
                Playlist4Changes.SelectedListContent.parser(), SELECTED_LIST_CONTENT_JSON_CONVERTER);
    }

    @NotNull
    public static ProtoJsonMercuryRequest<Metadata.Track> getTrack(@NotNull TrackId id) {
        return new ProtoJsonMercuryRequest<>(RawMercuryRequest.get(id.toMercuryUri()), Metadata.Track.parser(), TRACK_JSON_CONVERTER);
    }

    @NotNull
    public static ProtoJsonMercuryRequest<Metadata.Artist> getArtist(@NotNull ArtistId id) {
        return new ProtoJsonMercuryRequest<>(RawMercuryRequest.get(id.toMercuryUri()), Metadata.Artist.parser(), ARTIST_JSON_CONVERTER);
    }

    @NotNull
    public static ProtoJsonMercuryRequest<Metadata.Album> getAlbum(@NotNull AlbumId id) {
        return new ProtoJsonMercuryRequest<>(RawMercuryRequest.get(id.toMercuryUri()), Metadata.Album.parser(), ALBUM_JSON_CONVERTER);
    }

    @NotNull
    public static ProtobufMercuryRequest<Mercury.MercuryMultiGetReply> multiGet(@NotNull String uri, Mercury.MercuryRequest... subs) {
        RawMercuryRequest.Builder request = RawMercuryRequest.newBuilder()
                .setContentType("vnd.spotify/mercury-mget-request")
                .setMethod("GET")
                .setUri(uri);

        Mercury.MercuryMultiGetRequest.Builder multi = Mercury.MercuryMultiGetRequest.newBuilder();
        for (Mercury.MercuryRequest sub : subs)
            multi.addRequest(sub);

        request.addProtobufPayload(multi.build());
        return new ProtobufMercuryRequest<>(request.build(), Mercury.MercuryMultiGetReply.parser());
    }

    @NotNull
    public static JsonMercuryRequest<ResolvedContextWrapper> resolveContext(@NotNull String uri) {
        return new JsonMercuryRequest<>(RawMercuryRequest.get(String.format("hm://context-resolve/v1/%s", uri)), ResolvedContextWrapper.class);
    }

    @NotNull
    private static String getAsString(@NotNull JsonObject obj, @NotNull String key) {
        JsonElement elm = obj.get(key);
        if (elm == null) throw new NullPointerException("Unexpected null value for " + key);
        else return elm.getAsString();
    }

    @Contract("_, _, !null -> !null")
    private static String getAsString(@NotNull JsonObject obj, @NotNull String key, @Nullable String fallback) {
        JsonElement elm = obj.get(key);
        if (elm == null) return fallback;
        else return elm.getAsString();
    }

    public static final class ResolvedContextWrapper extends JsonWrapper {

        public ResolvedContextWrapper(@NotNull JsonElement elm) {
            super(elm);
        }

        @NotNull
        public JsonArray pages() {
            return obj().getAsJsonArray("pages");
        }

        @NotNull
        public JsonObject metadata() {
            return obj().getAsJsonObject("metadata");
        }

        @NotNull
        public String uri() {
            return getAsString(obj(), "uri");
        }

        @NotNull
        public String url() {
            return getAsString(obj(), "url");
        }
    }
}
