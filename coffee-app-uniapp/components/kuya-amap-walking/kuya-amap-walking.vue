<template>
    <view 
        id="amap"
        style="width: 100%; height: 100%"
        :points="points"
        :change:points="aMapRenderJs.receivePoints"
    >
    </view>
</template>

<script>
export default {
    props: {
        startPoint: {
            type: Array,
            default: () => [],
        },
        endPoint: {
            type: Array,
            default: () => [],
        },
    },
    computed: {
        points () {
            return { startPoint: this.startPoint, endPoint: this.endPoint };
        }
    }
};
</script>

<script module="aMapRenderJs" lang="renderjs">
const A_MAP_KEY = "";
const A_MAP_SECRET_KEY = "";

export default {
    mounted () {
        if (typeof window.AMap === 'function') {
            this.initAmap();
        } else {
            window._AMapSecurityConfig = {
                securityJsCode: A_MAP_SECRET_KEY,
            };
            const script = document.createElement('script');
            script.src = `https://webapi.amap.com/maps?v=1.4.15&key=${A_MAP_KEY}&plugin=AMap.Walking`;
            script.onload = this.initAmap.bind(this);
            document.head.appendChild(script);
        }
    },
    methods: {
        initAmap () {
            const map = new AMap.Map('amap', {
                resizeEnable: true,
            });
            // 开始绘画路径
            const walking = new AMap.Walking({
                map: map,
            });

            this.map = map;
            this.walking = walking;

            this.makeWalking();
        },
        makeWalking() {
            if(!this.points.startPoint.length || !this.points.endPoint.length) {
                return;
            };

            this.walking.search(
                this.points.startPoint,
                this.points.endPoint,
                function(status, result) {
                    if (status === 'complete') {
                        console.log('绘制步行路线完成');
                    } else {
                        console.error('步行路线数据查询失败' + result);
                    }
                }
            );
        },
        receivePoints(newVal) {
            console.log(`坐标更新：${JSON.stringify(newVal)}`);
            this.makeWalking();
        },
    },
};
</script>
