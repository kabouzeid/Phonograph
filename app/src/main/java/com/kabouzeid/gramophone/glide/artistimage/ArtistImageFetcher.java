package com.kabouzeid.gramophone.glide.artistimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.kabouzeid.gramophone.deezer.rest.DeezerRestClient;
import com.kabouzeid.gramophone.deezer.rest.model.DeezerArtist;
import com.kabouzeid.gramophone.deezer.rest.model.DeezerResponse;
import com.kabouzeid.gramophone.glide.audiocover.AudioFileCoverUtils;
import com.kabouzeid.gramophone.util.ImageUtil;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.PreferenceUtil;
import retrofit2.Response;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistImageFetcher implements DataFetcher<InputStream> {

    private final ArtistImage model;
    private InputStream stream;
    private Context context;

    private volatile boolean isCancelled;

    private final DeezerRestClient deezerRestClient;
    private DataFetcher<InputStream> urlFetcher;
    private ModelLoader<GlideUrl, InputStream> urlLoader;
    private final int width;
    private final int height;


    public ArtistImageFetcher(final Context context, final ArtistImage model, final DeezerRestClient deezerRestClient, ModelLoader<GlideUrl, InputStream> urlLoader, int width, int height) {
        this.context = context;
        this.model = model;
        this.deezerRestClient = deezerRestClient;
        this.urlLoader = urlLoader;
        this.width = width;
        this.height = height;

    }

    @Override
    public String getId() {
        // makes sure we never ever return null here
        return String.valueOf(model.artistName);
    }

    @Override
    public InputStream loadData(Priority priority) throws Exception {

        if (!MusicUtil.isArtistNameUnknown(model.artistName) && PreferenceUtil.isAllowedToDownloadMetadata(context)) {

            final Response<DeezerResponse<DeezerArtist>> response = deezerRestClient.getApiService().getArtistInfo(model.artistName, null).execute();

            if (!response.isSuccessful()) {
                throw new IOException("Request failed with code: " + response.code());
            }

            if (isCancelled) return null;

            final DeezerResponse<DeezerArtist> deezerResponse = response.body();

            if (deezerResponse != null && deezerResponse.getData() != null && deezerResponse.getData().size() > 0) {

                DeezerArtist correctArtist = null;
                for (final DeezerArtist artist : deezerResponse.getData()) {
                    if (model.artistName.equalsIgnoreCase(artist.getName())) {
                        correctArtist = artist;
                        break;
                    }
                }

                if (correctArtist == null) {
                    correctArtist = deezerResponse.getData().get(0);
                }

                final String picture_medium = correctArtist.getPicture_medium();
                GlideUrl url = new GlideUrl(picture_medium);

                final Uri urii = Uri.parse(picture_medium);

                if (!"artist".equals(urii.getPathSegments().get(urii.getPathSegments().size() - 2))) {
                    urlFetcher = urlLoader.getResourceFetcher(url, width, height);
                    stream = urlFetcher.loadData(priority);
                }
            }
        }

        if (stream == null) {
            stream = getMosaic(model.albumCovers);
        }
        return stream;
    }

    private InputStream getMosaic(final List<AlbumCover> albumCovers) throws FileNotFoundException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        int artistBitMapSize = 512;

        final Map<InputStream, Integer> images = new HashMap<>();

        InputStream result = null;
        List<InputStream> streams = new ArrayList<>();

        try {
            for (final AlbumCover cover : albumCovers) {

                retriever.setDataSource(cover.getFilePath());
                byte[] picture = retriever.getEmbeddedPicture();
                final InputStream stream;
                if (picture != null) {
                    stream = new ByteArrayInputStream(picture);
                } else {
                    stream = AudioFileCoverUtils.fallback(cover.getFilePath());
                }

                if (stream != null) {
                    images.put(stream, cover.getYear());
                }
            }

            int nbImages = images.size();

            if (nbImages > 3) {
                streams = new ArrayList<>(images.keySet());

                int divisor = 1;
                for (int i = 1; i < nbImages && Math.pow(i, 2) <= nbImages; ++i) {
                    divisor = i;
                }
                divisor += 1;
                double nbTiles = Math.pow(divisor, 2);

                if (nbImages < nbTiles) {
                    divisor -= 1;
                    nbTiles = Math.pow(divisor, 2);
                }
                final int resize = (artistBitMapSize / divisor) + 1;

                final Bitmap bitmap = Bitmap.createBitmap(artistBitMapSize, artistBitMapSize, Bitmap.Config.RGB_565);

                final Canvas canvas = new Canvas(bitmap);

                int x = 0;
                int y = 0;

                for (int i = 0; i < streams.size() && i < nbTiles; ++i) {
                    final Bitmap bitmap1 = ImageUtil.resize(streams.get(i), resize, resize);
                    canvas.drawBitmap(bitmap1, x, y, null);
                    x += resize;

                    if (x >= artistBitMapSize) {
                        x = 0;
                        y += resize;
                    }
                }

                final ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
                result = new ByteArrayInputStream(bos.toByteArray());

            } else if (nbImages > 0) {
                // we return the latest cover album of the artist
                Map.Entry<InputStream, Integer> maxEntryYear = null;

                for (final Map.Entry<InputStream, Integer> entry : images.entrySet()) {
                    if (maxEntryYear == null || entry.getValue().compareTo(maxEntryYear.getValue()) > 0) {
                        maxEntryYear = entry;
                    }
                }

                if (maxEntryYear != null) {
                    result = maxEntryYear.getKey();
                } else {
                    result = images.entrySet().iterator().next().getKey();
                }

            }
        } finally {
            retriever.release();
            try {
                for (final InputStream stream : streams) {
                    stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return result;
    }

    @Override
    public void cleanup() {

        // already cleaned up in loadData and ByteArrayInputStream will be GC'd
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ignore) {
                // can't do much about it
            }
        }


        if (urlFetcher != null) {
            urlFetcher.cleanup();
        }

    }

    @Override
    public void cancel() {
        isCancelled = true;
        if (urlFetcher != null) {
            urlFetcher.cancel();
        }
    }
}
