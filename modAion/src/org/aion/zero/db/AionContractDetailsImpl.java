/*******************************************************************************
 * Copyright (c) 2017-2018 Aion foundation.
 *
 *     This file is part of the aion network project.
 *
 *     The aion network project is free software: you can redistribute it
 *     and/or modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation, either version 3 of
 *     the License, or any later version.
 *
 *     The aion network project is distributed in the hope that it will
 *     be useful, but WITHOUT ANY WARRANTY; without even the implied
 *     warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *     See the GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with the aion network project source files.
 *     If not, see <https://www.gnu.org/licenses/>.
 *
 *
 * Contributors:
 *     Aion foundation.
 *     
 ******************************************************************************/

package org.aion.zero.db;

import static org.aion.base.util.ByteArrayWrapper.wrap;
import static org.aion.base.util.ByteUtil.EMPTY_BYTE_ARRAY;
import static org.aion.crypto.HashUtil.EMPTY_TRIE_HASH;
import static org.aion.crypto.HashUtil.h256;

import java.util.*;

import org.aion.base.db.IByteArrayKeyValueStore;
import org.aion.base.db.IContractDetails;
import org.aion.base.type.Address;
import org.aion.base.util.ByteArrayWrapper;
import org.aion.mcf.db.AbstractContractDetails;
import org.aion.mcf.ds.XorDataSource;
import org.aion.mcf.vm.types.DataWord;
import org.aion.rlp.RLP;
import org.aion.rlp.RLPElement;
import org.aion.rlp.RLPItem;
import org.aion.rlp.RLPList;
import org.aion.mcf.trie.merkle.SecureTrie;

public class AionContractDetailsImpl extends AbstractContractDetails<DataWord> {

    private IByteArrayKeyValueStore dataSource;

    private byte[] rlpEncoded;

    private Address address = Address.EMPTY_ADDRESS();

    private Set<ByteArrayWrapper> keys = new HashSet<>();
    private SecureTrie storageTrie = new SecureTrie(null);

    public boolean externalStorage;
    private IByteArrayKeyValueStore externalStorageDataSource;

    public AionContractDetailsImpl() {
        super(-1, 1000000);
    }

    public AionContractDetailsImpl(int prune, int memStorageLimit) {
        super(prune, memStorageLimit);
    }

    private AionContractDetailsImpl(Address address, SecureTrie storageTrie, Map<ByteArrayWrapper, byte[]> codes) {
        this.address = address;
        this.storageTrie = storageTrie;
        setCodes(codes);
    }

    public AionContractDetailsImpl(byte[] code) throws Exception {
        if (code == null) {
            throw new Exception("Empty input code");
        }

        decode(code);
    }

    private void addKey(byte[] key) {
        keys.add(wrap(key));
    }

    private void removeKey(byte[] key) {
        // keys.remove(wrap(key)); // TODO: we can't remove keys , because of
        // fork branching
    }

    @Override
    public void put(DataWord key, DataWord value) {
        if (value.equals(DataWord.ZERO)) {
            storageTrie.delete(key.getData());
            removeKey(key.getData());
        } else {
            storageTrie.update(key.getData(), RLP.encodeElement(value.getNoLeadZeroesData()));
            addKey(key.getData());
        }

        this.setDirty(true);
        this.rlpEncoded = null;
    }

    @Override
    public DataWord get(DataWord key) {
        DataWord result = DataWord.ZERO;

        byte[] data = storageTrie.get(key.getData());
        if (data.length > 0) {
            byte[] dataDecoded = RLP.decode2(data).get(0).getRLPData();
            result = new DataWord(dataDecoded);
        }

        return result;
    }

    @Override
    public byte[] getStorageHash() {
        return storageTrie.getRootHash();
    }

    @Override
    public void decode(byte[] rlpCode) {
        RLPList data = RLP.decode2(rlpCode);
        RLPList rlpList = (RLPList) data.get(0);

        RLPItem address = (RLPItem) rlpList.get(0);
        RLPItem isExternalStorage = (RLPItem) rlpList.get(1);
        RLPItem storage = (RLPItem) rlpList.get(2);
        RLPElement code = rlpList.get(3);
        RLPList keys = (RLPList) rlpList.get(4);
        RLPItem storageRoot = (RLPItem) rlpList.get(5);

        if (address.getRLPData() == null) {
            this.address = Address.EMPTY_ADDRESS();
        } else {
            this.address = Address.wrap(address.getRLPData());
        }

        this.externalStorage = !Arrays.equals(isExternalStorage.getRLPData(), EMPTY_BYTE_ARRAY);
        this.storageTrie.deserialize(storage.getRLPData());
        if (code instanceof RLPList) {
            for (RLPElement e : ((RLPList) code)) {
                setCode(e.getRLPData());
            }
        } else {
            setCode(code.getRLPData());
        }
        for (RLPElement key : keys) {
            addKey(key.getRLPData());
        }

        if (externalStorage) {
            storageTrie.withPruningEnabled(prune >= 0);
            storageTrie.setRoot(storageRoot.getRLPData());
            storageTrie.getCache().setDB(getExternalStorageDataSource());
        }

        externalStorage = (storage.getRLPData().length > detailsInMemoryStorageLimit) || externalStorage;

        this.rlpEncoded = rlpCode;
    }

    @Override
    public byte[] getEncoded() {
        if (rlpEncoded == null) {

            byte[] rlpAddress = RLP.encodeElement(address.toBytes());
            byte[] rlpIsExternalStorage = RLP.encodeByte((byte) (externalStorage ? 1 : 0));
            byte[] rlpStorageRoot = RLP.encodeElement(externalStorage ? storageTrie.getRootHash() : EMPTY_BYTE_ARRAY);
            byte[] rlpStorage = RLP.encodeElement(storageTrie.serialize());
            byte[][] codes = new byte[getCodes().size()][];
            int i = 0;
            for (byte[] bytes : this.getCodes().values()) {
                codes[i++] = RLP.encodeElement(bytes);
            }
            byte[] rlpCode = RLP.encodeList(codes);
            byte[] rlpKeys = RLP.encodeSet(keys);

            this.rlpEncoded = RLP.encodeList(rlpAddress, rlpIsExternalStorage, rlpStorage, rlpCode, rlpKeys,
                    rlpStorageRoot);
        }

        return rlpEncoded;
    }

    @Override
    public Map<DataWord, DataWord> getStorage(Collection<DataWord> keys) {
        Map<DataWord, DataWord> storage = new HashMap<>();
        if (keys == null) {
            for (ByteArrayWrapper keyBytes : this.keys) {
                DataWord key = new DataWord(keyBytes);
                DataWord value = get(key);

                // we check if the value is not null,
                // cause we keep all historical keys
                if (value != null) {
                    storage.put(key, value);
                }
            }
        } else {
            for (DataWord key : keys) {
                DataWord value = get(key);

                // we check if the value is not null,
                // cause we keep all historical keys
                if (value != null) {
                    storage.put(key, value);
                }
            }
        }

        return storage;
    }

    @Override
    public Map<DataWord, DataWord> getStorage() {
        return getStorage(null);
    }

    @Override
    public int getStorageSize() {
        return keys.size();
    }

    @Override
    public Set<DataWord> getStorageKeys() {
        Set<DataWord> result = new HashSet<>();
        for (ByteArrayWrapper key : keys) {
            result.add(new DataWord(key));
        }
        return result;
    }

    @Override
    public void setStorage(List<DataWord> storageKeys, List<DataWord> storageValues) {

        for (int i = 0; i < storageKeys.size(); ++i) {
            put(storageKeys.get(i), storageValues.get(i));
        }
    }

    @Override
    public void setStorage(Map<DataWord, DataWord> storage) {
        for (DataWord key : storage.keySet()) {
            put(key, storage.get(key));
        }
    }

    @Override
    public Address getAddress() {
        return address;
    }

    @Override
    public void setAddress(Address address) {
        this.address = address;
        this.rlpEncoded = null;
    }

    public SecureTrie getStorageTrie() {
        return storageTrie;
    }

    @Override
    public void syncStorage() {
        if (externalStorage) {
            storageTrie.withPruningEnabled(prune >= 0);
            storageTrie.getCache().setDB(getExternalStorageDataSource());
            storageTrie.sync();
        }
    }

    public void setDataSource(IByteArrayKeyValueStore dataSource) {
        this.dataSource = dataSource;
    }

    private IByteArrayKeyValueStore getExternalStorageDataSource() {
        if (externalStorageDataSource == null) {
            externalStorageDataSource = new XorDataSource(dataSource,
                    h256(("details-storage/" + address.toString()).getBytes()));
        }
        return externalStorageDataSource;
    }

    public void setExternalStorageDataSource(IByteArrayKeyValueStore dataSource) {
        this.externalStorageDataSource = dataSource;
        this.externalStorage = true;
    }

    @Override
    public IContractDetails<DataWord> clone() {

        // FIXME: clone is not working now !!!
        // FIXME: should be fixed
        // storageTrie.getRoot();

        return new AionContractDetailsImpl(address, storageTrie, getCodes());
    }

    @Override
    public IContractDetails<DataWord> getSnapshotTo(byte[] hash) {

        IByteArrayKeyValueStore keyValueDataSource = this.storageTrie.getCache().getDb();

        SecureTrie snapStorage = wrap(hash).equals(wrap(EMPTY_TRIE_HASH))
                ? new SecureTrie(keyValueDataSource, "".getBytes())
                : new SecureTrie(keyValueDataSource, hash);
        snapStorage.withPruningEnabled(storageTrie.isPruningEnabled());

        snapStorage.setCache(this.storageTrie.getCache());

        AionContractDetailsImpl details = new AionContractDetailsImpl(this.address, snapStorage, getCodes());
        details.externalStorage = this.externalStorage;
        details.externalStorageDataSource = this.externalStorageDataSource;
        details.keys = this.keys;
        details.dataSource = dataSource;

        return details;
    }
}
