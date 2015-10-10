package com.kelsos.mbrc.di.modules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.kelsos.mbrc.di.providers.BusProvider;
import com.kelsos.mbrc.di.providers.ObjectMapperProvider;
import com.kelsos.mbrc.di.providers.OkHttpClientProvider;
import com.kelsos.mbrc.di.providers.RemoteApiProvider;
import com.kelsos.mbrc.interactors.PlayerInteractor;
import com.kelsos.mbrc.interactors.TrackCoverInteractor;
import com.kelsos.mbrc.interactors.TrackInfoInteractor;
import com.kelsos.mbrc.interactors.TrackRatingInteractor;
import com.kelsos.mbrc.interactors.PlayerInteractorImpl;
import com.kelsos.mbrc.interactors.TrackCoverInteractorImpl;
import com.kelsos.mbrc.interactors.TrackInfoInteractorImpl;
import com.kelsos.mbrc.interactors.TrackRatingInteractorImpl;
import com.kelsos.mbrc.models.MainViewModel;
import com.kelsos.mbrc.models.MainViewModelImpl;
import com.kelsos.mbrc.presenters.MainViewPresenter;
import com.kelsos.mbrc.presenters.MiniControlPresenter;
import com.kelsos.mbrc.presenters.interfaces.IMainViewPresenter;
import com.kelsos.mbrc.presenters.interfaces.IMiniControlPresenter;
import com.kelsos.mbrc.repository.TrackRepository;
import com.kelsos.mbrc.repository.TrackRepositoryImpl;
import com.kelsos.mbrc.rest.RemoteApi;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.otto.Bus;

@SuppressWarnings("UnusedDeclaration") public class RemoteModule extends AbstractModule {
  public void configure() {
    bind(ObjectMapper.class).toProvider(ObjectMapperProvider.class).asEagerSingleton();
    bind(OkHttpClient.class).toProvider(OkHttpClientProvider.class).in(Singleton.class);
    bind(RemoteApi.class).toProvider(RemoteApiProvider.class).asEagerSingleton();
    bind(Bus.class).toProvider(BusProvider.class).in(Singleton.class);
    bind(IMiniControlPresenter.class).to(MiniControlPresenter.class);
    bind(IMainViewPresenter.class).to(MainViewPresenter.class);
    bind(MainViewModel.class).to(MainViewModelImpl.class);

    bind(TrackInfoInteractor.class).to(TrackInfoInteractorImpl.class);
    bind(TrackRatingInteractor.class).to(TrackRatingInteractorImpl.class);
    bind(TrackCoverInteractor.class).to(TrackCoverInteractorImpl.class);
    bind(PlayerInteractor.class).to(PlayerInteractorImpl.class);
    bind(TrackRepository.class).to(TrackRepositoryImpl.class);
  }
}
