/*=========================================================================
 * Copyright (c) 2002-2014 Pivotal Software, Inc. All Rights Reserved.
 * This product is protected by U.S. and international copyright
 * and intellectual property laws. Pivotal products are covered by
 * more patents listed at http://www.pivotal.io/patents.
 *=========================================================================
 */
   
   
package com.gemstone.gemfire.internal.admin.remote;

import com.gemstone.gemfire.distributed.internal.*;
import com.gemstone.gemfire.*;
import com.gemstone.gemfire.cache.RegionAttributes;
import com.gemstone.gemfire.internal.admin.*;
import com.gemstone.gemfire.internal.i18n.LocalizedStrings;
import java.io.*;

/**
 * A message that is sent to a particular application to get the
 * region for the specified path.
 * @since 3.5
 */
public final class RegionRequest extends AdminRequest {

  /** Request to get a region */
  static final int GET_REGION = 10;

  /** Request to create a VM root region */
  static final int CREATE_VM_ROOT = 11;

  /** Request to create a VM region */
  static final int CREATE_VM_REGION = 12;

  //////////////////////  Instance Fields  //////////////////////

  /** The action to be taken by this request */
  int action = 0;

  /** The id of the Cache in the recipient VM */
  int cacheId = 0;

  /** The path to the region requested or operated on */
  String path;

  /** The name of region to create */
  String newRegionName;

  /** The attributes for the region to create */
  RegionAttributes newRegionAttributes;

  //////////////////////  Static Methods  ///////////////////////

  /**
   * Returns a <code>RegionRequest</code> for getting a region with
   * the given name.
   *
   * @param c
   *        The admin object for the remote cache
   * @param path
   *        The full path to the region
   */
  public static RegionRequest createForGet(CacheInfo c, String path) {
    RegionRequest m = new RegionRequest();
    m.action = GET_REGION;
    m.cacheId = c.getId();
    m.path = path;
    RegionRequest.setFriendlyName(m);
    return m;
  }

  /**
   * Returns a <code>RegionRequest</code> for creating a VM root
   * region with the given name and attributes. 
   */
  public static RegionRequest createForCreateRoot(CacheInfo c,
                                                  String name,
                                                  RegionAttributes attrs) {
    RegionRequest m = new RegionRequest();
    m.action = CREATE_VM_ROOT;
    m.cacheId = c.getId();
    m.newRegionName = name;
    m.newRegionAttributes = new RemoteRegionAttributes(attrs);
    RegionRequest.setFriendlyName(m);
    return m;
  }

  /**
   * Returns a <code>RegionRequest</code> for creating a VM root
   * region with the given name and attributes. 
   */
  public static RegionRequest 
    createForCreateSubregion(CacheInfo c, String parentPath,
                             String name, RegionAttributes attrs) {
    RegionRequest m = new RegionRequest();
    m.action = CREATE_VM_REGION;
    m.cacheId = c.getId();
    m.path = parentPath;
    m.newRegionName = name;
    m.newRegionAttributes = new RemoteRegionAttributes(attrs);
    RegionRequest.setFriendlyName(m);
    return m;
  }

  public RegionRequest() {
    RegionRequest.setFriendlyName(this);
  }

  /**
   * Must return a proper response to this request.
   */
  @Override
  protected AdminResponse createResponse(DistributionManager dm) {
    // nothing needs to be done. If we got this far then a cache must exist.
    return RegionResponse.create(dm, this.getSender(), this);
  }

  public int getDSFID() {
    return REGION_REQUEST;
  }

  @Override
  public void toData(DataOutput out) throws IOException {
    super.toData(out);
    out.writeInt(this.action);
    out.writeInt(this.cacheId);
    DataSerializer.writeString(this.path, out);
    DataSerializer.writeString(this.newRegionName, out);
    DataSerializer.writeObject(this.newRegionAttributes, out);
  }

  @Override
  public void fromData(DataInput in)
    throws IOException, ClassNotFoundException {
    super.fromData(in);
    this.action = in.readInt();
    this.cacheId = in.readInt();
    this.path = DataSerializer.readString(in);
    this.newRegionName = DataSerializer.readString(in);
    this.newRegionAttributes =
      (RegionAttributes) DataSerializer.readObject(in);
    RegionRequest.setFriendlyName(this);
  }

  @Override
  public String toString() {
    return "RegionRequest from " + getRecipient() + " path=" + this.path;
  }
  
  private static void setFriendlyName(RegionRequest rgnRqst) {
    switch (rgnRqst.action) {
      case GET_REGION:
        rgnRqst.friendlyName = LocalizedStrings.RegionRequest_GET_A_SPECIFIC_REGION_FROM_THE_ROOT.toLocalizedString();
        break;
      case CREATE_VM_ROOT:
        rgnRqst.friendlyName = LocalizedStrings.RegionRequest_CREATE_A_NEW_ROOT_VM_REGION.toLocalizedString();
        break;
      case CREATE_VM_REGION:
        rgnRqst.friendlyName = LocalizedStrings.RegionRequest_CREATE_A_NEW_VM_REGION.toLocalizedString();
        break;
      default:
        rgnRqst.friendlyName = LocalizedStrings.RegionRequest_UNKNOWN_OPERATION_0.toLocalizedString(Integer.valueOf(rgnRqst.action));
        break;
      }
  }
}
