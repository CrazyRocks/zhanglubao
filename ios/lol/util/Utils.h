//
//  UrlUtil.h
//  lol
//
//  Created by Rocks on 15/9/6.
//  Copyright (c) 2015年 Zhanglubao.com. All rights reserved.
//
#import <Foundation/Foundation.h>
#import "ZLBAPI.h"

#undef	ZLBURL
#define ZLBURL(URL)		[NSString stringWithFormat:@"%@%@", api_domain, URL]

@interface Utils: NSObject


@end
